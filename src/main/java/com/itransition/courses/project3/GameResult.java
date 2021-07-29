package com.itransition.courses.project3;

public enum GameResult {
    DRAW,
    WIN,
    LOSE;

    @Override
    public String toString() {
        if (this.equals(WIN)) return "You win";
        else if (this.equals(LOSE)) return "You lost";
        else return "It's a tie";
    }
}
