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

## Google Play との接続を確立する

BillingClient.startConnection(BillingClientStateListener) を最初に呼び出す。

## 購入可能なアイテムを表示する

BillingClient.querySkuDetailsAsync を利用する。
呼び出しの前には BillingClient.startConnection(BillingClientStateListener) を呼び出し、 BillingClientStateListener.onBillingSetupFinished まで進んでいる必要がある。

## 購入フローを起動する

BillingClient.launchBillingFlow(Activity, BillingFlowParams) を呼び出す。

実際に手元で確認するときには、以下の手順が必要かもしれない（しない場合、課金のダイアログは開くが `the item you requested is not available for purchase` とエラーが出て課金できない）。

- 内部テスト・トラック等にテスターとしてメールアドレスを登録する
- (内部テストにアップロードしたアプリのパッケージ名が動作確認しようとしているアプリのパッケージ名と同じである？)
    - [StackOverflow](https://stackoverflow.com/questions/13117081/the-item-you-requested-is-not-available-for-purchase) で「署名付きのビルドをすること」などと書いてあるが、そこまでしなくてもアイテムを表示できた
- テスターとして登録したメールアドレスで Google Play および Chrome にログインする

![購入フローを起動する](./launch-the-purchase-flow.png)

テスト購入では実際には課金されない。

メールに記載されている Order number と Purchase.orderId が一致する。
Order number をインデックスとして払い戻し対応等を行う。

## 購入を処理する

ユーザーが購入した後に、アプリケーション（＋サーバー）で実際に購入を処理する必要がある。
そうしなければ、購入はキャンセルされる。

購入を処理するプロセスは消費不可アイテムの購入、消費可能アイテムの購入、定期購入の3つのパターンがある。

- 消費可能アイテムの場合、 BillingClient.consumeAsync(ConsumeParams,ConsumeResponseListener) を呼び出す。
- 消費不可能アイテムまたは定期購入アイテムの場合、 BillingClient.acknowledgePurchase(AcknowledgePurchaseParams,AcknowledgePurchaseResponseListener) を呼び出す。

消費可能アイテムと消費不可能アイテムは、Google Play Console 上で定義されるものではない。
単に、利用側が consumeAsync を呼び出すのか acknowledgePurchase を呼び出すのかでしか区別されない。
そのため、ひとつのアイテムを消費可能アイテムとして利用し、何かをトリガーに消費不可能アイテムとして処理してしまうことも可能である。

## Appendix

- [公式サンプルGitHubプロジェクト](https://github.com/android/play-billing-samples)

### 消費可能アイテム・消費不可アイテム・定期購入

消費可能アイテム・消費不可アイテム・定期購入と[ドキュメント](https://developer.android.com/google/play/billing/terminology#content-types)で呼び分けられている。
しかし、正直これらの分け方はドキュメント上、 Play Console 上、実装上それぞれで一貫しておらず、わかりにくい作りになっている。
ここでそれぞれにおける扱いを分けて記載する。

| | 消費可能アイテム | 消費不可アイテム | 定期購入 |
| :-- | :-- | :-- | :-- |
| [Android Developer での用語](https://developer.android.com/google/play/billing/terminology#content-types) | `1回限りのアイテム` に分類される。 | `1回限りのアイテム` に分類される。 | `定期購入` に分類される。 |
| Play Console | `アプリ内サービス` の `アプリ内アイテム` と呼ばれる。消費可能アイテムと区別することはできない（そのような設定項目はない）。 | `アプリ内サービス` の `アプリ内アイテム` と呼ばれる。消費可能アイテムと区別することはできない（そのような設定項目はない）。 | `定期購入` と呼ばれる。 |
| [Play Console ヘルプ](https://support.google.com/googleplay/android-developer/answer/1153481?hl=ja&ref_topic=3452890) | `管理対象アイテム` と呼ばれる。消費不可アイテムとの区別はない。 | `管理対象アイテム` と呼ばれる。消費可能アイテムとの区別はない。 | `定期購入` と呼ばれる。
| [BillingClient.SkuType](https://developer.android.com/reference/com/android/billingclient/api/BillingClient.SkuType) | `SkuType.INAPP` | `SkuType.INAPP` | `SkuType.SUBS` |
| [SkuDetails](https://developer.android.com/reference/com/android/billingclient/api/SkuDetails) の特徴 | 消費不可アイテムと区別する方法はない。 | 消費可能アイテムと区別する方法はない。 | SkuDetails.getType() を見て区別可能。 |
| 購入の処理 | BillingClient.consymeAsync | BillingClient.acknowledgePurchase | BillingClient.acknowledgePurchase |

上記を見ての通り、基本的に消費可能アイテムと消費不可アイテムを区別する仕組みはない。
別途 product id と消費可否の紐付けを管理する必要がある。

#### Purchase のフラグとアイテムの分類

|`getPurchaseState()` | `isAcknowledged()` | `isAutoRenewing()` | 分類 |
|:--|:--|:--| :-- |
| `PENDING` | `true` | `true` | 分類なし（のはず） |
| `PENDING` | `true` | `false` | 分類なし（のはず） |
| `PENDING` | `false` | `true` | 購入処理前の定期購入 |
| `PENDING` | `false` | `false` | 購入処理前の消費可能アイテム・消費不可アイテム |
| `PURCHASED` | `true` | `true` | 購入処理後の定期購入 |
| `PURCHASED` | `true` | `false` | 購入処理後の消費不可アイテム |
| `PURCHASED` | `false` | `true` | 分類なし（のはず) |
| `PURCHASED` | `false` | `false` | 購入処理後の消費可能アイテム |

※ `PurchaseState.UNSPECIFIED_STATE` は例外ケースにあたると考えられるため省略