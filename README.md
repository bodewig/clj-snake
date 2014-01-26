clj-snake
=========

Yet another simple snake game written in Clojure

This is more a project to practice Clojure for myself than something I
expect to be useful for anybody else.  It stands on the shoulders of
the many Snake games published before it and is heavily influenced by
Stuart Halloway's Snake [example Snake
program](https://github.com/stuarthalloway/programming-clojure/blob/master/src/examples/snake.clj).

The far end goal (beyond practicing) for myself is creating a
replacement for the KSnake game that was dropped from KDE 4 and is
missed badly by a person close to me.

So beyond the simple Snake game it should provide

* apples can rot
* an exit opens once all apples have been consumed, the game is only
  won once the player's snake leaves the board via this exit
* new apples are added when all apples have been eaten and time runs
  out
* a second game-controlled snake with some limited AI that tries to
  steal apples and even hit the player's snake
* a ball bouncing around, getting hit by it is deadly for the player's
  snake
* walls inside the board that need to be run around
* several levels


