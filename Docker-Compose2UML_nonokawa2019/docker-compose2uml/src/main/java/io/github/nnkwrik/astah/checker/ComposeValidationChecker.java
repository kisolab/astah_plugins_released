package io.github.nnkwrik.astah.checker;

import io.github.nnkwrik.astah.exception.ComposeCheckException;
import io.github.nnkwrik.astah.exception.ComposeCheckException.Level;
import io.github.nnkwrik.astah.exception.ComposeCheckException.Type;
import io.github.nnkwrik.astah.model.Compose;
import io.github.nnkwrik.astah.model.Element;
import io.github.nnkwrik.astah.model.Service;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 妥当性チェッカー
 * <p>
 * ワーニングを示す箇所
 * •サービスが孤立している
 * •サービス間の依存がループになっている
 * <p>
 * エラーを示す箇所
 * •同じ名前のサービスが存在する
 * •他のサービスとポートが被っている
 * •既存でないネットワークに依存している
 *
 * @author Reika Nonokawa
 */
public class ComposeValidationChecker {

    private static ComposeValidationChecker instance;

    /**
     * インスタンスを取得
     *
     * @return
     */
    public static ComposeValidationChecker getInstance() {
        if (instance == null) {
            synchronized (ComposeValidationChecker.class) {
                if (instance == null) {
                    instance = new ComposeValidationChecker();
                }
            }
        }
        return instance;
    }

    /**
     * すべての項目についてチェックする
     *
     * @param compose
     * @throws ComposeCheckException
     */
    public void check(Compose compose) throws ComposeCheckException {
        checkServiceIsolation(compose);
        checkServiceDependsLoop(compose);
        checkDuplicatedServiceName(compose);
        checkDuplicatedServicePorts(compose);
        checkUndifinedNetwork(compose);
    }

    /**
     * 孤立しているサービスのチェック
     *
     * @param compose
     * @throws ComposeCheckException ワーニング
     */
    public void checkServiceIsolation(Compose compose) throws ComposeCheckException {
        List<Service> services = compose.getServices();
        Set<Service> isolated = compose.getServices().stream().collect(Collectors.toSet());//copy

        for (Service service : services) {

            Set<Service> depends = service.getDependsOrLinksService().keySet();

            if (depends.size() > 0) {
                isolated.remove(service);
            }
            depends.forEach(d -> isolated.remove(d));
        }

        if (isolated.size() > 0) {
            StringBuilder sb = new StringBuilder();
            isolated.stream()
                    .map(Element::getName)
                    .forEach(name -> {
                        sb.append("'" + name + "',");
                    });
            sb.deleteCharAt(sb.length() - 1);//remove last ","
            throw new ComposeCheckException(Type.VALIDATION, Level.WARNING,
                    "サービス(" + sb.toString() + ") が依存関係を持たない孤立したサービスです。");
        }
    }

    /**
     * サービス間の依存がループになっているのチェック
     *
     * @param compose
     * @throws ComposeCheckException ワーニング
     */
    public void checkServiceDependsLoop(Compose compose) throws ComposeCheckException {
        List<Service> services = compose.getServices();
        //dfsアルゴリズム
        Set<Service> visited = new HashSet<>();
        Set<Service> recStack = new HashSet<>();
        for (Service service : services) {
            if (detectLoop(service, visited, recStack)) {
                throw new ComposeCheckException(Type.VALIDATION, Level.WARNING,
                        "サービス'" + service.getName() + "'について、依存関係のループが存在します。" +
                                "'depends_on'または'links'について確認してください");
            }
        }
    }

    /**
     * dfsアルゴリズムで依存のループを検出
     *
     * @param service
     * @param visited
     * @param recStack
     * @return
     */
    private boolean detectLoop(Service service, Set<Service> visited, Set<Service> recStack) {
        if (recStack.contains(service)) {
            return true;
        }

        if (visited.contains(service)) {
            return false;
        }

        visited.add(service);
        if (service.getDependsOrLinksService() == null) {
            return false;
        }
        recStack.add(service);

        Set<Service> depends = service.getDependsOrLinksService().keySet();
        for (Service depend : depends) {
            if (detectLoop(depend, visited, recStack)) {
                return true;
            }
        }

        recStack.remove(service);
        return false;

    }

    /**
     * 同じ名前のサービスが存在するのチェック
     *
     * @param compose
     * @throws ComposeCheckException エラー
     */
    public void checkDuplicatedServiceName(Compose compose) throws ComposeCheckException {
        List<Service> services = compose.getServices();

        Set<String> duplicates = new LinkedHashSet<>();
        Set<String> uniques = new HashSet<>();


        for (Service service : services) {
            if (!uniques.add(service.getName())) {
                duplicates.add(service.getName());
            }
        }

        if (duplicates.size() > 0) {
            StringBuilder sb = new StringBuilder();
            duplicates.forEach(name -> {
                sb.append("'" + name + "'");
                sb.append(",");
            });
            sb.deleteCharAt(sb.length() - 1);//remove last ","

            throw new ComposeCheckException(Type.VALIDATION, Level.ERROR,
                    "サービス (" + sb.toString() + ") と重複した名前のサービスが存在します。起動後にエラーが発生する恐れがあります");
        }
    }

    /**
     * 他のサービスとポートが被っているのチェック
     *
     * @param compose
     * @throws ComposeCheckException エラー
     */
    public void checkDuplicatedServicePorts(Compose compose) throws ComposeCheckException {
        List<Service> services = compose.getServices();

        Set<String> duplicates = new LinkedHashSet<>();
        Set<String> uniques = new HashSet<>();

        for (Service service : services) {
            if (service.getPorts() == null || service.getPorts().size() <= 0) {
                continue;
            }
            for (Element port : service.getPorts()) {
                String disposed = port.getName().split("/")[0].split(":")[0];

                if (!uniques.add(disposed)) {
                    duplicates.add(disposed);
                }
            }
        }

        if (duplicates.size() > 0) {
            StringBuilder sb = new StringBuilder();
            duplicates.forEach(port -> {
                sb.append("'" + port + "'");
                sb.append(",");
            });

            sb.deleteCharAt(sb.length() - 1);//remove last ","

            throw new ComposeCheckException(Type.VALIDATION, Level.ERROR,
                    "複数のサービスがホストポート (" + sb.toString() + ") にコンテナポートを公開しています。");
        }

    }


    /**
     * 既存でないネットワークに依存しているのチェック
     *
     * @param compose
     * @throws ComposeCheckException エラー
     */
    public void checkUndifinedNetwork(Compose compose) throws ComposeCheckException {
        List<String> definedNetwork = compose.getNetworks().stream()
                .map(Element::getName)
                .collect(Collectors.toList());
        List<Service> services = compose.getServices();

        for (Service service : services) {
            if (service.getNetworks() == null || service.getNetworks().size() <= 0) {
                continue;
            }
            for (String network : service.getNetworks()) {
                if (!definedNetwork.contains(network)) {
                    throw new ComposeCheckException(Type.VALIDATION, Level.ERROR,
                            "サービス(" + service.getName() + ")がnetworksで未定義のネットワーク(" + network + ")を使用しています");
                }
            }
        }
    }


}
