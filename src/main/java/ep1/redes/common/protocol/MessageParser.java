package common.protocol;

import java.util.Arrays;

import common.game.Board;
import common.game.Direction;
import common.game.Position;
import common.game.TileType;
import common.protocol.message_types.*;

public class MessageParser {

    public static Message parse(String serializedMessage) {
        String[] parts = serializedMessage.split(",", -1);
        MessageType type = MessageType.valueOf(parts[0]);

        int dataLength = parts.length - 1;
        if (parts[parts.length - 1].equals("\\")) {
            dataLength--;
        }

        String[] data = new String[dataLength];
        System.arraycopy(parts, 1, data, 0, dataLength);


        //usei essa sintaxe para ficar mais curto, ficou até fofo
        return switch (type) {
            case JOIN -> new JoinMessage(data[0]);
            case CONNECTED -> new ConnectedMessage(data[0], Integer.parseInt(data[1]));
            case READY -> new ReadyMessage();
            case WAITING -> new WaitingMessage();
            case LOBBY_FULL -> new LobbyFullMessage();
            case GAME_START -> new GameStartMessage(position(data, 0));
            case MOVE -> new MoveMessage(Direction.valueOf(data[0]));
            case SHOOT -> new ShootMessage(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
            case MOVE_RESULT -> new MoveResultMessage(position(data, 0));
            case SHOOT_RESULT -> new ShootResultMessage(
                    position(data, 0),
                    TileType.valueOf(data[2]));
            case GAME_STATE -> gameState(data);
            case ROUND -> new RoundMessage(
                    Integer.parseInt(data[0]),
                    Boolean.parseBoolean(data[1]),
                    Boolean.parseBoolean(data[2]));
            case GAME_OVER -> new GameOverMessage(Integer.parseInt(data[0]), data[1]);
            case ERROR -> new ErrorMessage(data[0]);
            default -> new ErrorMessage("Mensagem Recebida é Invalida");
        };
    }

    private static Position position(String[] data, int offset) {
        return new Position(
                Integer.parseInt(data[offset]),
                Integer.parseInt(data[offset + 1]));
    }

    private static GameStateMessage gameState(String[] data) {
        Position playerPosition = position(data, 0);
        int width = Integer.parseInt(data[2]);
        int height = Integer.parseInt(data[3]);
        TileType[][] visibleTiles = new TileType[height][width];

        for (TileType[] row : visibleTiles) {
            Arrays.fill(row, TileType.FOG);
        }


        /*ficou confuso então irei explicar:
        O data é um array formado por trios, o primeiro indice do trio sinaliza a posição x
        o segundo indice sinaliza a posição y
        o terceiro sinaliza o valor da célula (x,y) (agua, névoa ou jogador);
        por isso, o indice i pula de 3 em 3, ele vai sempre pro próximo trio

        Isso foi feito para evitar enviar a matriz inteira na mensagem (eu me arrependi disso, talvez tenha ficado complicado sem motivo e nem tenha de fato ficado mais leve).
        */
        for (int i = 4; i < data.length; i += 3) {
            int x = Integer.parseInt(data[i]);
            int y = Integer.parseInt(data[i + 1]);
            visibleTiles[x][y] = TileType.valueOf(data[i + 2]);
        }

        return new GameStateMessage(new Board(playerPosition, visibleTiles));
    }
}
