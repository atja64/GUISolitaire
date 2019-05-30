# solitaire

A simple version of [Solitaire](https://en.wikipedia.org/wiki/Patience_(game)) implemented in Java using JavaFX.

## Instructions

* Use the mouse to select cards.
* When a card or stack of cards is selected it will be highlighted in blue.
* With card(s) selected click on another card to try and move that card, if the move isn't valid you'll receive a warning otherwise the card will be moved.
* If a stack of cards is selected they will all be moved.
* Click the hand (top left) to reveal a new card.

## Rules

* The goal is to make four stacks of identically suited cards in the foundations (top right four spaces).
* You do this by moving cards around the board and from the waste (top left space) to the board.
* On the board you can only place a card on a card with value one higher than it and of a different colour.
* All but the top cards on the board are hidden at the start of the game. Move the card on top of a hidden card to reveal it.
* Only kings can be placed on empty spaces on the board and only aces on the foundations.


## Current Features

* A fully working version of solitaire!
* Move cards
* Cards are highlighted when you click on them
* You can win the game

## To-Do

* Add move hints
* Add auto-completion
* Add draw-three option

## Licensing

This software is licensed under the MIT License. Please see the [License](LICENSE) file for more information.
