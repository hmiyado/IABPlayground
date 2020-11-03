# Google Play Billing Library をアプリに統合する

## 購入ライフサイクル

TBD

## 購入トークンとオーダーID

TBD

## エラー処理

TBD

## Google Play に接続する

エミュレーターで動作確認するときは、 Google Play アプリが端末に入っていること＋Play にログインしていることが必要。
これをしない場合、「Play In-app Billing ライブラリのバージョンが古い」といったエラーメッセージがでる。

## Google Play Billing Library への依存関係を追加する

書いてあるとおりのライブラリを導入する。

## BillingClient を初期化する

[BillingClient](https://developer.android.com/reference/com/android/billingclient/api/BillingClient) の newBuilder を使う。
BillingClient.startConnection(BillingClientStateListener) を最初に呼び出す。
BillingClientStateListener.onBillingSetupFinished が呼び出されると BillingClient.querySkuDetailsAsync などをできるようになる。

## Appendix

- [公式サンプルGitHubプロジェクト](https://github.com/android/play-billing-samples)
