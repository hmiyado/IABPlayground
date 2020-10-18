# 準備する

## Google Play デベロッパーアカウントを設定する

Google Play Console で支払いプロファイルを設定する。

まず、プロファイル作成直前の画面が以下。
![支払いプロファイルの作成](./create-billing-profile-1.png)

「お支払いプロファイルを作成」を押すと、設定画面に遷移する。

![支払いプロファイルの設定画面1](./create-billing-profile-2.png)
![支払いプロファイルの設定画面1](./create-billing-profile-3.png)
![支払いプロファイルの設定画面1](./create-billing-profile-4.png)

個人のお支払いプロファイルに紐付けるかこのために新しく作るかや、会社なのか個人なのかといった設定項目がある。

設定を完了すると以下の画面に遷移する。
![支払いプロファイルの設定画面1](./create-billing-profile-5.png)


## Google Play Console で請求関連の機能を有効にする

### AAB でリリースビルドを作る

Android App Bundle （AAB）でリリースビルドを作る。

```sh
$ ./gradlew clean
$ ./gradlew bundleRelease
```

`app/build/outputs/bundle/release` に `app-release.aab` が生成されるのでこれを使う。 