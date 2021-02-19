-----------------プラグインについて-----------------
＠ src/main/java/jp : m2tベースのコード生成に関する部分

・CodeGenerator
主にgenerateメソッドへの事前構造検査追加
utils.setmap()が解析情報の引き渡し部分

・Generatorcheck
提案手法により作成した生成用の解析を行うクラス
内容はほぼsrc/main/java/umlcheker/UMLchek.javaのクローン

・GeneratorUtils
生成用テンプレートに図要素を引き渡すためのクラス
setmapメソッドで図要素を引き渡す
その他，生成用テンプレートで使用するためのメソッドを追加

＊他は特にいじってない(覚えてる限りでは)

＠ src/main/java/umlcheker : 挿入支援，構造検査に関する部分

・Activator
プロジェクト内の要素選択といったイベントの取得が主
挿入支援の際，選択フローうんぬんで使用

・ChangedSliceColor
スライスのハイライトに使用

・CreateTemplate
テンプレート挿入クラス
差し込み型挿入は開始終了間にあるノード・フローのコピーだが，
出現するノードの型は全てに対応していないので
テンプレートで使うノードによっては追加する必要がある
挟み込み型挿入はテンプレートの図に印を入れ，
それを見て決め打ちで生成しているので拡張性は最悪
テンプレート構造の内部ステートメントにあたる部分に印付けをすれば解消できそう

・DiagramReflection
エラーノートの生成など
reflactionEvaluationメソッドのswitch文の各caseだが，
(たぶん)astahまたはAPIのバージョン違いから上手く動かないかも

・Dialog
主に挿入支援の際のwarningのアラート用

・EachNote
ノートの内容はここ

・ExtensionView
拡張タブに関する部分
図要素の変更も拾ってる

・Modelinsert
構造挿入支援の前準備
別に必要ない

・NodeGenerator
アクティビティ図の各要素を生成する
何かの名残で別に必要ない

・ProjectChangedAction
プロジェクトの変更をキャッチしてどうこうしようとした名残
バックグラウンド検査に使うなど発展させられる部分はある

・Relay
なにかのために作ったクラス
なにかの変数を保持しているようだ

・Serialize
生成ノート情報をシリアライズし，プロジェクトをいったん閉じた後でも
ノートの追跡をするために作成した模様
あまり機能してないかも

・SliceViewer
スライスのハイライトのための前準備
別に必要ない

・UMLcheck
本命，構造解析部分
probnextnodeメソッドでフロー探査
nstackに探索するノードを入れたり出したりして探査する
記述規則を満たしていれば構図判定，ダメそうならエラーとしてマークする
ノード，フロー，各記述規則の組み合わせによって漏れる可能性大，
記述規則を満たしておらず不整合として検出されるはずが，
それを拾う部分が抜けており，NullPointerやEmptyStackが度々起こる可能性大

＠ java.template
resources/m2t/templates/java.template
m2tのgit(https://github.com/s-hosoai/astahm2t)の概要を要見られたし

-----------------注意事項-----------------
・コードを生成する際，astahのプロジェクト名と生成するクラス図の名前あるいはクラス名が
一致していないと生成されないかもしれない
この辺はm2tの仕様と思われるので改修する必要がある
・astah-buildにより生成したjarファイルをastahに入れ動かす際，うまく動かない可能性がある
astah-launchでは動くのでjarにする際に問題があると思われる(特にm2t周り)
VScodeで開発を行ったが，Eclipseの方が良いかも
-----------------参考になるサイト-----------------
↓を見ながらAPIを使用(もっと新しいバージョンがあるかも)
http://members.change-vision.com/javadoc/astah-api/7_2_0/api/ja/doc/javadoc/com/change_vision/jude/api/inf/model/IElement.html

なんか色々載ってるやつ
https://gist.github.com/y-matsuda/7c06762eca6c4d4f6539
