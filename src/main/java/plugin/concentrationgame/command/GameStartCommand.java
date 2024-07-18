package plugin.concentrationgame.command;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import plugin.concentrationgame.Main;
import plugin.concentrationgame.PlayerScoreData;
import plugin.concentrationgame.data.PairEntityData;
import plugin.concentrationgame.mapper.data.PlayerScore;

/**
 * 神経衰弱ゲームを開始するコマンドです。
 * プレイヤーの周囲にエンティティが出現し、各エンティティにペア番号が設定されます。
 * 選択した2つのエンティティのペア番号が同じ場合、得点が加算されます。
 * 結果はプレイヤー名、点数、日時で保存されます。
 */
public class GameStartCommand extends BaseCommand implements Listener {
  private final Main main;
  public static final String LIST = "list";
  private final PlayerScoreData playerScoreData = new PlayerScoreData();
  private static final int PAIR_COUNT = 8;
  private int gameTime = 60;
  private int score = 0;
  private List<Entity> spawnEntityList = new ArrayList<>();
  private List<PairEntityData> pairEntityDataList = new ArrayList<>();
  private boolean entitySelect = false;
  private Entity[] pairEntityArray = new Entity[2];
  private int[] selectPairNumberArray = new int[2];

  public GameStartCommand(Main main) {
    this.main = main;
  }


  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label, String[] args) {
    if (args.length == 1 && LIST.equals(args[0])) {
      sendPlayerScoreList(player);
      return false;
    }
    player.sendMessage("神経衰弱ゲームを開始します。");
    gameTime = 60;
    score = 0;

    spawnEntityPair(player);
    gamePlay(player);
    return true;
  }

  /**
   * 現在登録されているスコアの一覧をメッセージ表示します。
   * @param player コマンドを実行したプレイヤー
   */
  private void sendPlayerScoreList(Player player) {
    List<PlayerScore> playerScoreList = playerScoreData.selectList();
    for (PlayerScore playerScore : playerScoreList) {
      player.sendMessage(playerScore.getId() + " | "
          + playerScore.getPlayerName() + " | "
          + playerScore.getScore() + " | "
          + playerScore.getRegisteredAt()
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
      );
    }
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label,
      String[] args) {
    return false;
  }

  /**
   * ゲームを実行します。規定の時間内にペアのエンティティを選択すると、スコアが加算されます。
   * 合計スコアを時間経過後に表示します。
   * @param player コマンドを実行したプレイヤー
   */
  private void gamePlay(Player player) {
    Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
      if(gameTime <= 0) {
        Runnable.cancel();
        player.sendTitle("ゲームが終了しました。",player.getName() + " 合計" + score + "点！",0,60,0);

        spawnEntityList.forEach(Entity::remove);
        spawnEntityList.clear();
        pairEntityDataList.clear();
        pairArrayReset();
        entitySelect = false;

        playerScoreData.insert(new PlayerScore(player.getName(), score));

        return;
      }
      gameTime -= 5;
    },0,5 * 20);
  }

  /**
   * プレイヤーの周囲にエンティティを出現させ、各エンティティに対してペア番号を設定します。
   *
   * @param player コマンドを実行したプレイヤー
   */
  private void spawnEntityPair(Player player) {
    World world = player.getWorld();
    Location playerLocation = player.getLocation();

    for(int i = 1; i <= PAIR_COUNT; i++) {
      for(int j = 1; j <= 2; j++) {
        int randomX = new SplittableRandom().nextInt(16) - 8;
        int randomZ = new SplittableRandom().nextInt(16) - 8;
        double x = playerLocation.getX() + randomX;
        double y = playerLocation.getY();
        double z = playerLocation.getZ() + randomZ;

        Entity entity = world.spawnEntity(new Location(world, x, y, z), EntityType.MINECART_FURNACE);
        spawnEntityList.add(entity);
        PairEntityData pairEntityData = new PairEntityData(entity.getEntityId(), i);
        pairEntityDataList.add(pairEntityData);
      }
    }
  }



  @EventHandler
  public void onInteractEntity(PlayerInteractEntityEvent e) {
    Entity clickEntity = e.getRightClicked();
    Player player = e.getPlayer();
    if(spawnEntityList.stream().noneMatch(entity -> entity.equals(clickEntity))) {
      return;
    }

    if(!entitySelect) {
      pairEntityArray[0] = clickEntity;
      getPairNumber(0, player);
      entitySelect = true;
    } else {
      pairEntityArray[1] = clickEntity;

      if(pairEntityArray[0].equals(pairEntityArray[1])) {
        player.sendMessage("同じエンティティを選択しています。違うエンティティを選択してください。");
        pairEntityArray[1] = null;
        return;
      } else {
        getPairNumber(1, player);
      }

      if(selectPairNumberArray[0] == selectPairNumberArray[1]) {
        pairEntityArray[0].remove();
        pairEntityArray[1].remove();
        int point = 10;
        player.sendMessage(ChatColor.YELLOW + "ペアが成立しました！" + point + "点獲得！");
        score += point;
      } else {
        player.sendMessage("ペアは成立しませんでした。");
      }
      pairArrayReset();
      entitySelect = false;
    }
  }

  /**
   * プレイヤーが選択したエンティティおよびペア番号の配列を初期化します。
   */
  private void pairArrayReset() {
    pairEntityArray[0] = null;
    pairEntityArray[1] = null;
    selectPairNumberArray[0] = 0;
    selectPairNumberArray[1] = 0;
  }

  /**
   * クリックしたエンティティのペア番号を取得し、配列への格納およびペア番号の画面表示を行います。
   * @param x 格納する配列の添え字
   * @param player コマンドを実行したプレイヤー
   */
  private void getPairNumber(int x, Player player) {
    for (PairEntityData pairEntityData : pairEntityDataList) {
      if (pairEntityArray[x].getEntityId() == pairEntityData.getEntityId()) {
        selectPairNumberArray[x] = pairEntityData.getPairNumber();
        player.sendMessage("選択エンティティのペア番号は「" + selectPairNumberArray[x] + "」です");
      }
    }
  }
}
