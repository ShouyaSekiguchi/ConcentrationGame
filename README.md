# MineCraft版 神経衰弱ゲーム

## ゲーム概要
Entityで神経衰弱を行い、制限時間内により多くのペアを見つけ、スコアを獲得するゲームです。
- **ゲーム開始方法**: 任意の箇所で`/gamestart`コマンドを入力すると、ゲームが開始されます。
- **制限時間**: 60秒
- **生成されるEntityペア数**: 8

## ゲーム詳細
1. **Entityの出現**: ゲームを開始すると、16個のEntity(かまど付きトロッコ)がプレイヤーの周囲に出現します。
2. **Entityの選択**: 出現したEntityを右クリックすると、Entityが選択され、設定されているペア番号が表示されます。
3. **ペア判定**: 手順2で右クリックしたものと異なるEntityを右クリックすると、ペアの判定が行われます。
   1. **ペアが成立した場合**: 選択したEntityのペアが消滅し、10点がスコアに加算されます。
   2. **ペアが成立しなかった場合**: ペアが成立しなかったメッセージが表示され、Entityが選択されていない状態に戻ります。
4. **ゲーム終了**: 制限時間が経過するとゲームが終了し、出現したEntityは全て削除されます。ゲーム結果を画面に表示し、プレイヤー名、スコア、日時をDBに保存します。

## ゲームスコアの表示
- `/gamestart list`コマンドを入力すると、DBに保存されているスコア一覧が表示されます。

## DB接続(MySql)詳細
| 属性      | 設定値                   |
|---------|-----------------------|
| ユーザー名   | ※                     |
| パスワード   | ※                     |
| URL     | ※                     |
| データベース名 | spigot_server         |
| テーブル名   | minigame_player_score |
- （※）は自身のローカル環境に合わせてご使用ください。(mybatis-config.xmlで設定します)
### データベース・テーブル作成
MySqlに接続し、以下のコマンドを上から順に実行してください。
```
CREATE DATABASE spigot_server;

USE spigot_server;

CREATE TABLE minigame_player_score(id int auto_increment, player_name varchar(100), 
score int, registered_at datetime, primary key(id)) DEFAULT CHARSET=utf8;
```

## 対応バージョン
Spigot: 1.20.4  
Minecraft: 1.20.4  
※ 動作確認はWindowsでのみ実施しております。