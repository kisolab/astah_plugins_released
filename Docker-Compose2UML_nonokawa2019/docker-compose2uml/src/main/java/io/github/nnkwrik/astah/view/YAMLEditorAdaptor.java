package io.github.nnkwrik.astah.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.nnkwrik.astah.astah.ComposeDiagramManager;
import io.github.nnkwrik.astah.checker.ComposeConfirmer;
import io.github.nnkwrik.astah.constants.V3Configuration;
import io.github.nnkwrik.astah.model.Compose;
import io.github.nnkwrik.astah.model.Element;
import io.github.nnkwrik.astah.model.Service;
import io.github.nnkwrik.astah.compose.parser.ComposeYamlParser;
import io.github.nnkwrik.astah.util.YamlUtil;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;

/**
 * YAMLエディターでの編集を全体に適応するアダプター
 *
 * @author Reika Nonokawa
 */
public class YAMLEditorAdaptor {


    /**
     * サービスを追加する
     *
     * @param newRaw 追加するサービスのYAML
     */
    public Element create(String newRaw) throws IOException {
        Compose compose = ComposeDiagramManager.getCompose();
        //全体のYAMLをJsonNodeにする
        JsonNode composeNode = YamlUtil.readTree(compose.getRaw());
        //servicesの部分のObjectNodeを得る
        ObjectNode servicesNode = (ObjectNode) composeNode.findValue(V3Configuration.SERVICES);
        if (servicesNode == null) {
            //現在のYAMLにservicesがない。servicesとnewRawを追加する
            ((ObjectNode) composeNode).set(V3Configuration.SERVICES, YamlUtil.readTree(newRaw));
        } else {
            //servicesの中にnewRawを追加する
            String newServiceName = YamlUtil.readTree(newRaw).fieldNames().next();
            servicesNode.set(newServiceName, YamlUtil.readTree(newRaw).findValue(newServiceName));
        }
        //チェックしたのち、Composeにパースする
        Compose newCompose = checkAndParse(compose.getName(), composeNode);

        if (newCompose != null) {
            //新しいcomposeをダイアグラムに示す
            ComposeDiagramManager.clearDiagram();
            ComposeDiagramManager.creatDiagram(newCompose);
        }
        return newCompose;
    }

    /**
     * サービスを削除する
     *
     * @param service
     */
    public Compose delete(Service service) throws IOException {
        Compose compose = ComposeDiagramManager.getCompose();
        //全体のYAMLをJsonNodeにする
        JsonNode composeNode = YamlUtil.readTree(compose.getRaw());
        ObjectNode servicesNode = (ObjectNode) composeNode.findValue(V3Configuration.SERVICES);
        //サービスのノードを削除する
        servicesNode.remove(service.getName());

        //削除するサービスに依存しているサービスのdepends_onの部分を削除する
        deleteDepends(servicesNode, service);
        //チェックしたのち、Composeにパースする
        Compose newCompose = checkAndParse(compose.getName(), composeNode);

        if (newCompose != null) {
            //新しいcomposeをダイアグラムに示す
            ComposeDiagramManager.clearDiagram();
            ComposeDiagramManager.creatDiagram(newCompose);
        }

        return newCompose;
    }

    /**
     * 全体かサービスについて編集する
     *
     * @param element
     * @param newRaw
     * @return
     */
    public Element edit(Element element, String newRaw) throws IOException {
        if (element instanceof Service) {
            //サービスの編集
            return editService((Service) element, newRaw);
        } else if (element instanceof Compose) {
            //全体の編集
            return editCompose(newRaw);
        } else {
            return null;
        }
    }

    /**
     * 全体のYAMLを編集する
     *
     * @param newRaw
     * @return
     */
    private Compose editCompose(String newRaw) throws IOException {
        //編集後のYAMLについてリファレンス検査を行う
        if (!ComposeConfirmer.confirmReference(newRaw)) {
            return null;
        }
        //compose名（通常ファイル名）を得る
        String name = ComposeDiagramManager.getCompose().getName();
        JsonNode composeNode = YamlUtil.readTree(newRaw);
        //新しいYAMLをComposeオブジェクトに変換する
        Compose newCompose = ComposeYamlParser.parse(name, composeNode);

        //Composeの妥当性を検証する
        if (!ComposeConfirmer.confirmValidation(newCompose)) {
            return null;
        }

        //新しいComposeを図に示す
        ComposeDiagramManager.clearDiagram();
        ComposeDiagramManager.creatDiagram(newCompose);

        return newCompose;
    }

    /**
     * サービスのYAMLを編集する
     *
     * @param service
     * @param newRaw
     * @return
     */
    private Service editService(Service service, String newRaw) throws IOException {
        Compose compose = ComposeDiagramManager.getCompose();
        //全体のYAMLをJsonNodeにする
        JsonNode composeNode = YamlUtil.readTree(compose.getRaw());

        String newServiceName = YamlUtil.readTree(newRaw).fieldNames().next();
        ObjectNode servicesNode = (ObjectNode) composeNode.findValue(V3Configuration.SERVICES);

        //元のサービスのYAML部分を新しいものに置き換える
        servicesNode.remove(service.getName());
        servicesNode.set(newServiceName, YamlUtil.readTree(newRaw).findValue(newServiceName));

        //サービス名を変更した場合は、他のサービスのdepends_onにも適応する
        if (!newServiceName.equals(service.getName())) {
            renameDepends(servicesNode, service, newServiceName);
        }

        //チェックしたのち、Composeにパースする
        Compose newCompose = checkAndParse(compose.getName(), composeNode);
        if (newCompose == null) {
            return null;
        }

        //もとのサービスの配置位置を得る
        Map<Service, Point2D> newLocation = getNewLocation(newCompose, service);
        //図に設置する
        ComposeDiagramManager.clearDiagram();
        ComposeDiagramManager.creatDiagram(newCompose, newLocation);

        //新しいサービスをエディターに設置するため、リターン
        Service newService = newCompose.getServices().stream()
                .filter(s -> s.getName().equals(newServiceName)).findAny().get();
        return newService;

    }


    /**
     * サービスが削除されたときdepends_onに適応する
     *
     * @param servicesNode
     * @param service      削除するサービス
     */
    private void deleteDepends(ObjectNode servicesNode, Service service) {
        Iterator<String> nameIt = servicesNode.fieldNames();
        //すべてのサービスについてイテレータ
        while (nameIt.hasNext()) {
            String next = nameIt.next();
            JsonNode serviceNode = servicesNode.findValue(next);
            JsonNode dependsOnNode = serviceNode.findValue(V3Configuration.SERVICES_DEPENDSON);
            if (dependsOnNode == null)
                continue;
            if (dependsOnNode.isArray()) {
                List<String> dependson = YamlUtil.readValue(dependsOnNode.toString(),
                        TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
                //削除するサービスに依存している
                if (dependson == null || dependson.size() < 1 || !dependson.contains(service.getName())) {
                    continue;
                }
                //削除する
                dependson.remove(service.getName());
                if (dependson.size() > 0) {
                    ((ObjectNode) serviceNode).replace(V3Configuration.SERVICES_DEPENDSON, YamlUtil.valueToTree(dependson));
                } else {
                    ((ObjectNode) serviceNode).remove(V3Configuration.SERVICES_DEPENDSON);
                }
            } else if (dependsOnNode.asText().equals(service.getName())) {
                ((ObjectNode) serviceNode).remove(V3Configuration.SERVICES_DEPENDSON);
            }

        }
    }

    /**
     * サービスがリネームされたときdepends_onに適応する
     *
     * @param servicesNode
     * @param service      リネームするサービス
     * @param newName      リネームする名前
     */
    private void renameDepends(ObjectNode servicesNode, Service service, String newName) {
        Iterator<String> nameIt = servicesNode.fieldNames();
        //すべてのサービスをイテレータ
        while (nameIt.hasNext()) {
            String next = nameIt.next();
            JsonNode serviceNode = servicesNode.findValue(next);
            //todo service_links
            JsonNode dependsOnNode = serviceNode.findValue(V3Configuration.SERVICES_DEPENDSON);
            if (dependsOnNode == null)
                continue;
            if (dependsOnNode.isArray()) {
                List<String> dependson = YamlUtil.readValue(dependsOnNode.toString(),
                        TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
                //リネームしたサービスに依存している
                if (dependson == null || dependson.size() < 1 || !dependson.contains(service.getName())) {
                    continue;
                }
                //depends_on箇所もリネームする
                dependson.set(dependson.indexOf(service.getName()), newName);
                ((ObjectNode) serviceNode).replace(V3Configuration.SERVICES_DEPENDSON, YamlUtil.valueToTree(dependson));
            } else if (dependsOnNode.asText().equals(service.getName())) {
                ((ObjectNode) serviceNode).set(V3Configuration.SERVICES_DEPENDSON, new TextNode(newName));
            }

        }
    }

    /**
     * サービスの配置位置を取得
     *
     * @param newCompose
     * @param service
     * @return
     */
    private Map<Service, Point2D> getNewLocation(Compose newCompose, Service service) {
        //編集前のサービスの位置を取得
        Map<Service, Point2D> location = ComposeDiagramManager.getServiceLocation();
        Map<Service, Point2D> newLocation = new HashMap<>();
        newCompose.getServices().forEach(s -> {
            Optional<Service> osOp = location.keySet().stream()
                    .filter(os -> s.getName().equals(os.getName())).findFirst();

            if (osOp.isPresent()) {
                //編集前に存在しているサービス
                Service oldService = osOp.get();
                newLocation.put(s, location.get(oldService));
                location.remove(oldService);
            } else {
                //リネームされたサービス。リネーム前の位置を取得
                newLocation.put(s, location.get(service));
                location.remove(service);
            }
        });
        return newLocation;
    }

    /**
     * リファレンスと妥当性について確認したのちComposeオブジェクトにパースする
     *
     * @param composeName
     * @param composeNode
     * @return
     * @throws IOException
     */
    private Compose checkAndParse(String composeName, JsonNode composeNode) throws IOException {
        //サービス追加後のcomposeNodeについてリファレンス検査する
        if (!ComposeConfirmer.confirmReference(YamlUtil.getRawText(composeNode))) {
            return null;
        }
        //composeNodeをComposeオブジェクトに変更する
        Compose newCompose = ComposeYamlParser.parse(composeName, composeNode);

        //composeの妥当性を検証する
        if (!ComposeConfirmer.confirmValidation(newCompose)) {
            return null;
        }
        return newCompose;
    }


}
