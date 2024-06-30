package plugin.concentrationgame.data;

import lombok.Getter;
import lombok.Setter;

/**
 * エンティティのIDと神経衰弱のペア番号を保存するオブジェクトです。
 */
@Getter
@Setter
public class PairEntityData {
  private int EntityId;
  private int PairNumber;

  public PairEntityData(int entityId, int pairNumber) {
    this.EntityId = entityId;
    this.PairNumber = pairNumber;
  }
}
