package uk.ac.aber.dcs.cs39440.tjamflowlines;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import uk.ac.aber.dcs.cs39440.tjamflowlines.model.Cell;
import uk.ac.aber.dcs.cs39440.tjamflowlines.model.Game;
import uk.ac.aber.dcs.cs39440.tjamflowlines.model.PuzzleGenerator;

//import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import static uk.ac.aber.dcs.cs39440.tjamflowlines.model.Game.*;

public class HelloApplication extends Application {

    final double TEXT_SIZE_MULTIPLIER = 0.06;

    final String NEW_GAME_BUTTON_TEXT = "New Game";
    final String HINT_BUTTON_TEXT = "Hint";
    final String RESTART_BUTTON_TEXT = "Restart";

    final int GOALS_PER_COLOUR = 2;

    /**
     * The number of coloured goal pairs.
     */
    int oolourCount;

    /**
     * Each element index = colour id.
     * Each element value = Color enum for colour.
     */
    final Color[] GOAL_TO_COLOUR = {
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.HOTPINK,
            Color.PURPLE,
            Color.CYAN,
            Color.ORANGE,
            Color.BROWN,
            Color.SALMON,
            Color.DARKRED,
            Color.DARKBLUE,
            Color.DARKGREEN,
            Color.DARKMAGENTA,
            Color.PALEVIOLETRED,
            Color.LIGHTBLUE,
            Color.LIGHTGREEN,
            Color.BEIGE,
            Color.BLACK,
            Color.OLIVE,
            Color.MAROON,
            Color.CRIMSON,
            Color.DARKGRAY
    };

    /**
     * The popup screen that appears when clicking the 'new game button'
     */
    StackPane popup;

    /**
     * The root node.
     */
    StackPane root;

    /**
     * vertical box containing the btnRow and gridPane
     */
    VBox vBox;

    /**
     * Horizontal box holding the 3 buttons
     */
    HBox btnRow;

    Component target;

    /**
     * GridPane holding all the components.
     */
    GridPane gridPane;

    /**
     * a 2d grid representing the grid with only the colours of goals in their positions,
     */
    int[][] goalColours;

    /**
     * goals[0][1] = colour 0's end goal component.
     * goals[0][0] = colour 0's start goal component.
     */
    Component[][] goals;

    /**
     * Each element index of ArrayList = colour id.
     * Each elem value = path of that colour.
     */
    ArrayList<LinkedList<Component>> paths;
    Scene scene;

    /**
     * This is a template tile, all other tiles on the GridPane have the same size as this.
     */
    Rectangle template;

    PuzzleGenerator puzzleGenerator;

    final int SIZE = 6;

    /**
     * This is only called when the application is launched.
     *
     * Initialises all the GUI nodes.
     * @param stage - the stage on which the scene will be the child of.
     */
    @Override
    public void start(Stage stage)
    {
        makeNewPuzzle();

        Region spacer1 = new Region();
        Region spacer2 = new Region(); //spacers are for gaps between the buttons, the space for which is bound to the size of the window.
        Button newGameButton = new Button(NEW_GAME_BUTTON_TEXT);
        Button hintButton = new Button(HINT_BUTTON_TEXT);
        Button restartButton = new Button(RESTART_BUTTON_TEXT);

        btnRow = new HBox(newGameButton, spacer1, hintButton, spacer2, restartButton); //the bottom of the screen holding the 3 buttons.

        gridPane = new GridPane();
        gridPane.setAlignment(Pos.TOP_LEFT);

        vBox = new VBox(gridPane, btnRow);
        vBox.setPrefSize(50 * SIZE, 50 * SIZE);

        root = new StackPane(vBox);

        template = new Rectangle();
        template.widthProperty().bind(Bindings.min(
                root.widthProperty(),
                root.heightProperty().subtract(Bindings.min(root.widthProperty(), root.heightProperty()).divide(SIZE).multiply(0.75))
        ).divide(SIZE)); //basically, either the width of the grid == width of window OR height of grid == height of window (minus the bottom row of buttons)

        template.heightProperty().bind(template.widthProperty()); //all nodes on grid will be square.

        btnRow.maxWidthProperty().bind(template.widthProperty().multiply(SIZE)); //the width of row of buttons == width of grid.
        btnRow.setAlignment(Pos.BASELINE_CENTER);

        spacer1.prefWidthProperty().bind(template.widthProperty().multiply(0.1* SIZE));
        spacer2.prefWidthProperty().bind(template.widthProperty().multiply(0.1* SIZE));

        makeButton(newGameButton, 0.33* SIZE, 0.75, TEXT_SIZE_MULTIPLIER);
        makeButton(hintButton, 0.21* SIZE, 0.75, TEXT_SIZE_MULTIPLIER);
        makeButton(restartButton, 0.26* SIZE, 0.75, TEXT_SIZE_MULTIPLIER);

        //brings up the popup menu
        newGameButton.setOnAction(event -> root.getChildren().add(popup));

        //give hint
        hintButton.setOnAction(event -> {
            Cell goal; Component component;
            for (int colour = 0; colour < oolourCount; colour++)
            {
                if (!paths.get(colour).isEmpty())
                {
                    component = paths.get(colour).getLast();
                    goal = Game.idToCell[(Game.startGoals[colour] == cellToId[component.x][component.y])? endGoals[colour] : startGoals[colour]];
                    if (!isNeighbour(paths.get(colour).getFirst().x, paths.get(colour).getFirst().y, goal.getCol(), goal.getRow()))
                    {
                        completePath(colour);
                        break;
                    }
                }
                else
                {
                    completePath(colour);
                    break;
                }
            }
        });

        //restart puzzle
        restartButton.setOnAction(event -> {
            Component component;
            for (int colour = 0; colour < oolourCount; colour++)
            {
                while (paths.get(colour).size() > 1)
                {
                    component = paths.get(colour).removeFirst();
                    gridPane.getChildren().remove(component.shape);
                    Game.colours[cellToId[component.x][component.y]] = NULL_INT_VALUE;
                }
                if (!paths.get(colour).isEmpty())
                {
                    paths.get(colour).clear();
                }
            }
        });

        initialiseBaseGridAndGoals();

        makePopup();
        scene = new Scene(root);
        URL cssURL = getClass().getResource("stylesheet.css");
        if (cssURL != null)
        {
            System.out.println("stylesheet found");
            scene.getStylesheets().add(cssURL.toExternalForm());
        }
        else
        {
            System.out.println("stylesheet not found");
        }
        stage.setScene(scene);
        stage.show();

    }

    /**
     * Makes the grid's backgrounds and adds the goals to the grid.
     */
    public void initialiseBaseGridAndGoals() {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                Rectangle rect = new Rectangle();
                rect.setFill(((x + y) % 2 == 0) ? Color.LIGHTGRAY : Color.GRAY);

                rect.widthProperty().bind(template.widthProperty());
                rect.heightProperty().bind(template.heightProperty());

                gridPane.add(rect, x, y);
            }
        }

        goals = new Component[oolourCount][GOALS_PER_COLOUR];
        //add goals onto goalColours and gui.
        for (int colour = 0; colour < oolourCount; colour++)
        {
            Cell startGoal = Game.idToCell[Game.startGoals[colour]];
            Cell endGoal = Game.idToCell[Game.endGoals[colour]];

            //

            goalColours[startGoal.getCol()][startGoal.getRow()] = colour;
            Game.colours[Game.cellToId[startGoal.getCol()][startGoal.getRow()]] = colour;

            goalColours[endGoal.getCol()][endGoal.getRow()] = colour;
            Game.colours[Game.cellToId[endGoal.getCol()][endGoal.getRow()]] = colour;

            //

            goals[colour][0] = new Component(new Rectangle(), startGoal.getCol(), startGoal.getRow(), colour);
            goals[colour][0].shape.setFill(GOAL_TO_COLOUR[colour]);

            goals[colour][1] = new Component(new Rectangle(), endGoal.getCol(), endGoal.getRow(), colour);
            goals[colour][1].shape.setFill(GOAL_TO_COLOUR[colour]);

            //

            goals[colour][0].shape.widthProperty().bind(template.widthProperty());
            goals[colour][0].shape.heightProperty().bind(template.heightProperty());

            goals[colour][1].shape.widthProperty().bind(template.widthProperty());
            goals[colour][1].shape.heightProperty().bind(template.heightProperty());

            //

            makeGoalDraggable(goals[colour][0]);
            makeGoalDraggable(goals[colour][1]);

            //

            gridPane.add(goals[colour][0].shape, goals[colour][0].x,goals[colour][0].y);
            gridPane.add(goals[colour][1].shape, goals[colour][1].x,goals[colour][1].y);
        }
    }

    /**
     * Makes a new puzzle using the PuzzleGenerator and resets the colours, goals and paths.
     */
    public void makeNewPuzzle() {
        puzzleGenerator = new PuzzleGenerator(SIZE, SIZE);
        puzzleGenerator.generatePuzzle();
        puzzleGenerator.postGeneration();
        oolourCount = Game.startGoals.length;
        Game.colours = new int[Game.size];
        Arrays.fill(Game.colours, NULL_INT_VALUE);
        goalColours = new int[SIZE][SIZE];

        paths = new ArrayList<>(oolourCount);
        for (int colour = 0; colour < oolourCount; colour++)
        {
            paths.add(new LinkedList<>());
        }
    }

    /**
     * Makes a goal node draggable (can create, remove and edit paths from it)
     *
     * When it drags onto neighbouring tiles, component to be created and added onto path.
     */
    public void makeGoalDraggable(Component goal)
    {
        goal.shape.setOnMouseDragged(event -> {

            int x = (int) ((event.getSceneX() - (event.getSceneX() % goal.shape.getWidth())) / goal.shape.getWidth());
            int y = (int) ((event.getSceneY() - (event.getSceneY() % goal.shape.getHeight())) / goal.shape.getHeight()); //x,y coordinates of new component.

            if (
                    paths.get(goal.colour).size()>1 &&
                    paths.get(goal.colour).get(1).x == x &&
                    paths.get(goal.colour).get(1).y == y
            )
            { //remove front of path

                Component first = paths.get(goal.colour).getFirst();
                if (first != null)
                {
                    paths.get(goal.colour).removeFirst();
                    Game.colours[Game.cellToId[first.x][first.y]] = NULL_INT_VALUE;
                    gridPane.getChildren().remove(first.shape);
                }
            }
            else if ( //add onto path.
                    x < SIZE && y < SIZE &&
                    (Game.colours[Game.cellToId[x][y]] == NULL_INT_VALUE) &&
                    (paths.get(goal.colour).isEmpty() || isNeighbour(x,y,paths.get(goal.colour).getFirst().x,paths.get(goal.colour).getFirst().y))
            )
            {
                if (paths.get(goal.colour).isEmpty())
                {
                    paths.get(goal.colour).addFirst(goal);
                }
                makeNewComponent(x,y,goal.colour, true);
            }
        });

        componentOnMouseReleased(goal);
    }

    /**
     * Completes the path of a given colour.
     */
    public void completePath(int colour)
    {
        Component component; int otherColour;
        while (paths.get(colour).size() > 1)
        {
            component = paths.get(colour).removeFirst();
            gridPane.getChildren().remove(component.shape);
            Game.colours[cellToId[component.x][component.y]] = Game.NULL_INT_VALUE;
        }
        paths.get(colour).clear();

        for (int node : puzzleGenerator.paths.get(colour).sequence)
        {
            if (node != endGoals[colour])
            {
                otherColour = Game.colours[node];
                if (otherColour != Game.NULL_INT_VALUE && otherColour != colour) //if another path intersects the correct path, remove elements of that path up to that point.
                {
                    while (!paths.get(colour).isEmpty())
                    {
                        component = paths.get(otherColour).removeFirst();
                        gridPane.getChildren().remove(component.shape);
                        Game.colours[cellToId[component.x][component.y]] = Game.NULL_INT_VALUE;
                        if (component.x == idToCell[node].getCol() && component.y == idToCell[node].getRow())
                        {
                            break;
                        }
                    }
                }

                makeNewComponent(
                        Game.idToCell[node].getCol(),
                        Game.idToCell[node].getRow(),
                        colour,
                        false
                );
            }
        }
    }

    /**
     * Makes a non-goal component draggable (can edit paths on it)
     * @param comp - the component
     */
    public void makeComponentDraggable(Component comp)
    {
        comp.shape.setOnMouseDragged(event -> {

            if (!paths.get(comp.colour).isEmpty() && (paths.get(comp.colour).getFirst().equals(comp) || (target != null && target.equals(comp))))
            {
                target = comp;

                int x = (int) ((event.getSceneX() - (event.getSceneX() % comp.shape.getWidth())) / comp.shape.getWidth());
                int y = (int) ((event.getSceneY() - (event.getSceneY() % comp.shape.getHeight())) / comp.shape.getHeight());

                if (
                        paths.get(comp.colour).size()>1 &&
                        paths.get(comp.colour).get(1).x == x &&
                        paths.get(comp.colour).get(1).y == y
                )
                { //remove first of path.

                    Component first = paths.get(comp.colour).getFirst();
                    if (first != null)
                    {
                        if (first.equals(comp) && paths.get(comp.colour).size() > 2)
                        {
                            Component second = paths.get(comp.colour).remove(1);
                            Game.colours[Game.cellToId[first.x][first.y]] = NULL_INT_VALUE;
                            first.setPos(second.x, second.y);
                            GridPane.setColumnIndex(first.shape, second.x);
                            GridPane.setRowIndex(first.shape, second.y);
                            gridPane.getChildren().remove(second.shape); //if comp == first and deleted, can't go on.
                        }
                        else
                        {
                            paths.get(comp.colour).removeFirst();
                            Game.colours[Game.cellToId[first.x][first.y]] = NULL_INT_VALUE;
                            gridPane.getChildren().remove(first.shape); //if comp == first and deleted, can't go on.
                        }
                    }
                }
                else if ( //else adding onto path.
                        !paths.get(comp.colour).isEmpty() &&
                                x < SIZE && y < SIZE &&
                                isNeighbour(x,y,paths.get(comp.colour).getFirst().x,paths.get(comp.colour).getFirst().y) &&
                                Game.colours[Game.cellToId[x][y]] == NULL_INT_VALUE
                )
                {
                    if (goalColours[x][y] != target.colour)
                    {
                        makeNewComponent(x,y,comp.colour, true);
                    }
                }

            }
        });

        componentOnMouseReleased(comp);
    }

    /**
     * When the mouse is released after dragging a component, if only the goal is left in the path, clear the path.
     * @param comp - the component
     */
    public void componentOnMouseReleased(Component comp) {
        comp.shape.setOnMouseReleased(event -> {
            if (paths.get(comp.colour).size()==1)
            {
                paths.get(comp.colour).removeFirst();
            }
        });
    }

    /**
     * Checks if two given nodes are neighbours to eachother.
     * @param x1 column of the 1st node.
     * @param y1 row of the 1st node.
     * @param x2 column of the 2nd node.
     * @param y2 row of the 2nd node.
     * @return true (are neighbours) . false (are not)
     */
    public boolean isNeighbour(int x1, int y1, int x2, int y2)
    {
        return (Math.abs(x1 - x2) + Math.abs(y1 - y2) == 1);
    }

    /**
     * Makes a new component.
     * @param x - column of the new component on the grid.
     * @param y - row of the new component on the grid.
     * @param colour - colour of the new component on the grid.
     * @param isDraggable - can paths be created from it? Only false when component is given as a hint.
     */
    public void makeNewComponent(int x, int y, int colour, boolean isDraggable)
    {
        Component c = new Component(new Rectangle(), x, y, colour);
        c.shape.setFill(GOAL_TO_COLOUR[colour]);
        if (isDraggable)
        {
            makeComponentDraggable(c);
        }
        c.shape.widthProperty().bind(template.widthProperty());
        c.shape.heightProperty().bind(template.heightProperty());
        gridPane.add(c.shape, x, y);
        paths.get(colour).addFirst(c);
        Game.colours[Game.cellToId[x][y]] = colour;
    }

    public static void main(String[] args) {
        launch();
    }

    /**
     * Makes the popup screen which shows after clicking 'new game' button.
     */
    public void makePopup()
    {
        popup = new StackPane();
        popup.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);"); //skim
        popup.setAlignment(Pos.CENTER);

        StackPane pane = new StackPane();
        pane.maxWidthProperty().bind(template.widthProperty().multiply(SIZE -1));
        pane.maxHeightProperty().bind(template.heightProperty().multiply(SIZE -1));
        pane.setStyle("-fx-background-color: rgb(50,50,50)");

        Label title = new Label("Start New Game");
        title.setTextAlignment(TextAlignment.CENTER);
        title.getStyleClass().add("label");
        title.styleProperty().bind(Bindings.createStringBinding(() -> {
            double size = Math.min(template.getWidth(), template.getHeight()) * 0.08 * this.SIZE; // Adjust scaling factor as needed
            return "-fx-font-size: " + size + "px;";
        }, template.widthProperty(), template.heightProperty()));
        pane.getChildren().add(title);
        StackPane.setAlignment(title, Pos.TOP_CENTER);

        Button confirmButton = new Button("Confirm");
        makeButton(confirmButton, 1.5, 0.75, 0.06);

        Button cancelButton = new Button("Cancel");
        makeButton(cancelButton, 1.5, 0.75, 0.06);

        HBox hBox = new HBox(cancelButton, confirmButton);
        hBox.setAlignment(Pos.BOTTOM_RIGHT);
        pane.getChildren().add(hBox);
        StackPane.setAlignment(hBox, Pos.BOTTOM_RIGHT);

//        Button tb = new Button("6");
//        tb.getStyleClass().add("circular-outline-button");
//        tb.minWidthProperty().bind(template.widthProperty().multiply(0.5));
//        tb.minHeightProperty().bind(template.heightProperty().multiply(0.5));
//        tb.maxWidthProperty().bind(template.widthProperty().multiply(0.5));
//        tb.maxHeightProperty().bind(template.heightProperty().multiply(0.5));
//        tb.styleProperty().bind(Bindings.createStringBinding(() -> {
//            double size = Math.min(tb.getWidth(), tb.getHeight()) * 0.07 * SIZE; // Adjust scaling factor as needed
//            return "-fx-font-size: " + size + "px;";
//        }, tb.widthProperty(), tb.heightProperty()));
//        tb.setOnAction(event -> {
//            tb.getStyleClass().remove("circular-outline-button");
//            tb.getStyleClass().add("circular-outline-button-enabled");
//        });
//        pane.getChildren().add(tb);

        cancelButton.setOnAction(event -> root.getChildren().remove(popup));

        confirmButton.setOnAction(event -> {
            makeNewPuzzle();

            gridPane.getChildren().clear();

            initialiseBaseGridAndGoals();

            root.getChildren().remove(popup);
        });

        popup.getChildren().add(pane);
    }

    /**
     * Makes a button by setting its size and styling.
     * @param button - the button to edit.
     * @param widthMultiplier - how wide the button will be in comparison to the template tile.
     * @param heightMultiplier - how tall the button will be in comparison to the template tile.
     * @param fontSizeMultiplier - how big the font size will be.
     */
    public void makeButton(Button button, double widthMultiplier, double heightMultiplier, double fontSizeMultiplier)
    {
        button.minWidthProperty().bind(template.widthProperty().multiply(widthMultiplier));
        button.minHeightProperty().bind(template.heightProperty().multiply(heightMultiplier));
        button.getStyleClass().add("button");
        button.styleProperty().bind(Bindings.createStringBinding(() -> {
            double size = Math.min(button.getWidth(), button.getHeight()) * fontSizeMultiplier * this.SIZE; // Adjust scaling factor as needed
            return "-fx-font-size: " + size + "px;";
        }, button.widthProperty(), button.heightProperty())); //autosizing text.
    }
}