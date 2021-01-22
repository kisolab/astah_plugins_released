package io.github.nnkwrik.astah.checker;

import io.github.nnkwrik.astah.action.ExportAction;
import io.github.nnkwrik.astah.exception.ComposeCheckException;
import io.github.nnkwrik.astah.exception.ComposeCheckException.Type;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * リファレンスチェッカー
 *
 * @author Reika Nonokawa
 */
public class ComposeReferenceChecker {

    private static volatile ComposeReferenceChecker instance;
    //pythonインタープリター
    private PythonInterpreter pyInterp;
    //pythonインタープリターのoutputStream
    private ByteArrayOutputStream errOut;

    /**
     * 初回取得時にインタープリターのセットアップ
     * @return
     */
    public static ComposeReferenceChecker getInstance() {
        if (instance == null) {
            synchronized (ComposeReferenceChecker.class) {
                if (instance == null) {
                    instance = new ComposeReferenceChecker();
                    try {
                        instance.setupPyInterp();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return instance;
    }


    /**
     * Pythonインタープリターのセットアップ
     * @throws IOException
     */
    private void setupPyInterp() throws IOException {

        long startSetup = System.currentTimeMillis();

        String jythonJarPath = getJythonJarPath();
        pyInterp = buildInterpEnv(jythonJarPath);

        //sys.pathのセット
        pyInterp.exec("import sys");
        pyInterp.exec("sys.path.append('__pyclasspath__/Lib')");
        pyInterp.exec("sys.path.append('__pyclasspath__/Lib/site-packages')");

        //docker-composeのライブラリーをインポート
        pyInterp.exec("from compose.cli.main import main");

        long finishSetup = System.currentTimeMillis();
        //セットアップにかかった時間
        System.out.println("jython setup timecost : " + (finishSetup - startSetup) + "ms");

    }

    /**
     * jython jarのpathを取得
     * @return
     * @throws IOException
     */
    private String getJythonJarPath() throws IOException {
        ClassLoader classLoader = ComposeReferenceChecker.class.getClassLoader();
        Properties projectProp = new Properties();
        projectProp.load(classLoader.getResourceAsStream("project.properties"));

        String jar = System.getProperties().getProperty("file.separator") + projectProp.getProperty("python.runner.jar");
        String path = URLDecoder.decode(ExportAction.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
                "UTF-8");
        path = path.replace("file:///", "");
        path = new File(path).getParent() + jar;

        return path;
    }

    /**
     * PythonInterpreterの設定を行う
     * @param jythonPath
     * @return
     * @throws UnsupportedEncodingException
     */
    private PythonInterpreter buildInterpEnv(String jythonPath) throws UnsupportedEncodingException {
        //jython jarのパスをPythonInterpreterに設置する
        Properties pythonProp = new Properties();
        pythonProp.setProperty("python.path", jythonPath);
        pythonProp.setProperty("python.home", jythonPath);
        pythonProp.setProperty("python.prefix", jythonPath);
        pythonProp.setProperty("java.class.path", jythonPath);

        PythonInterpreter.initialize(System.getProperties(), pythonProp, new String[]{});

        //resourcesディレクトリにあるPythonコードを得るため、クラスローダーを設置
        PySystemState.initialize();
        PySystemState state = new PySystemState();
        state.setClassLoader(ComposeReferenceChecker.class.getClassLoader());
        state.setdefaultencoding("utf-8");

        PythonInterpreter pythonInterpreter = new PythonInterpreter(null, state);

        //エラーのoutputのstreamを設置する
        errOut = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(errOut, true, "UTF-8");
        pythonInterpreter.setErr(printStream);


        return pythonInterpreter;
    }

    /**
     * rawのYAMLでチェックする
     * @param raw
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws ComposeCheckException
     */
    public void check(String raw) throws FileNotFoundException, UnsupportedEncodingException, ComposeCheckException {
        //システムのtempディレクトリにファイルを作成し、rawをアウトプットする
        String tmpDir = System.getProperty("java.io.tmpdir");

        String tmpFilePath = tmpDir + System.getProperty("file.separator") + "docker-compose-plugin.tmp";

        PrintWriter writer = new PrintWriter(tmpFilePath, "UTF-8");
        writer.println(raw);
        writer.close();

        check(new File(tmpFilePath));
    }

    /**
     * fileのYAMLをチェックする
     * @param file
     * @throws ComposeCheckException
     */
    public void check(File file) throws ComposeCheckException {

        //ファイルパスを修正
        String filePath = file.getPath();
        if (System.getProperty("file.separator").equals("\\")) {
            filePath = filePath.replace('\\', '/');
        }

        long startSetup = System.currentTimeMillis();

        //システムargsにdocker-compose -f xx config -qを設置し、検査プログラムを実行する.
        pyInterp.exec(
                "sys.argv = ['docker-compose','-f','"
                        + filePath + "','config','-q']\n" +
                        "main()");
        long finish = System.currentTimeMillis();

        System.out.println("reference check timecost : " + (finish - startSetup) + "ms");

        //errOutkからチェックで検出したエラーを取得
        String checkResult = new String(errOut.toByteArray(), StandardCharsets.UTF_8);

        if (checkResult != null && !checkResult.trim().equals("")) {
            errOut.reset();
            String[] errs = checkResult.split("\n");
            StringBuilder err = new StringBuilder();
            for (String e : errs) {
                if (e.contains("ERROR") ||
                        (!e.contains("ERROR") && !e.contains("WARNING") && !e.contains("DEBUG") && !e.contains("INFO"))) {
                    err.append(e);
                    err.append("\n");
                }
            }
            if (!err.toString().trim().equals("")) {
                //検出したエラーをComposeCheckExceptionで投げ出す
                throw new ComposeCheckException(Type.REFERENCE, err.toString());
            }
        }

    }


}
