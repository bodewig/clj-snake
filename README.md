clj-snake
=========

This game tries to clone the game play of KDE 3's KSnake game which
was dropped from KDE 4 but is missed badly by at least one person
close to me.

To be honest I don't expect this to be useful or even get looked at
outside of my family :-)

The Game
--------

You control the green "snake" using the cursor keys.  Your snake
enters the game through a door at the bottom while a game controlled
snake enters through the top.

Your goal is to eat the "apples" that have been scattered around the
level (red or yellow circles) before they go bad or the other snake
eats them.  Don't run into walls, the other snake or your own tail and
make sure the bouncing ball doesn't hit your snake's head.

If you eat the last apple the top door opens up again for a limited
time and you can use it to escape the level.  Once escaped you'll find
your snake in the next level.

Should all apples rot away before they have been eaten or should the
last apple be eaten by the game's snake fresh apples will be created.

History
-------

The main goal of this tiny project has shifted a little over time.

I started this project to practice Clojure more than to really get a
working game but at one point it was good enough to be actually fun to
play for a while.

The initial code base stands on the shoulders of the many Snake games
published before it and is heavily influenced by Stuart Halloway's
Snake [example Snake
program](https://github.com/stuarthalloway/programming-clojure/blob/master/src/examples/snake.clj).
It still shares the threading approach with Stuart's game but pretty
much everything else has been rewritten.
