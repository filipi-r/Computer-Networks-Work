package common.protocol.message_types;

/**
 * Servidor -> jogador: número da rodada e o que ele ainda pode fazer nela.
 */
public record RoundMessage(int round, boolean canMove, boolean canShoot) implements Message {

    @Override
    public MessageType type() {
        return MessageType.ROUND;
    }

    @Override
    public String[] data() {
        return new String[] {
            String.valueOf(round),
            String.valueOf(canMove),
            String.valueOf(canShoot)
        };
    }
}
