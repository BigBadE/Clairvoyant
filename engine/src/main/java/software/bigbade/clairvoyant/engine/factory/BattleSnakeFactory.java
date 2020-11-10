package software.bigbade.clairvoyant.engine.factory;

import software.bigbade.clairvoyant.engine.api.IBattlesnakePlayer;
import software.bigbade.clairvoyant.engine.network.NetworkPlayer;

import java.net.URL;

public class BattleSnakeFactory {
    public static IBattlesnakePlayer getBattlesnake(String url) {
        return new NetworkPlayer(url);
    }
}
