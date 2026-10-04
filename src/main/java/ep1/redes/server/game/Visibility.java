package server.game;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import common.game.Position;

public class Visibility {
    private final Map<Position, Integer> revealed = new HashMap<>();

    public void reveal(Collection<Position> cells, int rounds) {
        for (Position cell : cells) {
            revealed.merge(cell, rounds, Math::max);
        }
    }

    public boolean isRevealed(Position p) {
        return revealed.containsKey(p);
    }

    public Set<Position> revealedCells() {
        return revealed.keySet();
    }

    public void endRound() {
        Iterator<Map.Entry<Position, Integer>> it = revealed.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Position, Integer> entry = it.next();
            entry.setValue(entry.getValue() - 1);
            if (entry.getValue() <= 0) it.remove();
        }
    }
}
