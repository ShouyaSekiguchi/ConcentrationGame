package plugin.concentrationgame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import plugin.concentrationgame.mapper.data.PlayerScore;

public interface PlayerScoreMapper {

  @Select("select * from minigame_player_score")
  List<PlayerScore> selectList();

  @Insert("insert minigame_player_score(player_name, score, registered_at) values (#{playerName}, #{score}, now())")
  int insert(PlayerScore playerScore);

}