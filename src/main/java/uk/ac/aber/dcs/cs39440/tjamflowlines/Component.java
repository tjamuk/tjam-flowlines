package uk.ac.aber.dcs.cs39440.tjamflowlines;

import javafx.scene.shape.Rectangle;

public class Component {
    public Rectangle shape;
    public int x;
    public int y;
    public int colour;

    public Component(Rectangle shape, int x, int y, int colour) {
        this.shape = shape;
        this.x = x;
        this.y = y;
        this.colour = colour;
    }

    public boolean equals(Component component)
    {
        return (component.x == this.x && component.y == this.y);
    }

    @Override
    public String toString() {
        return String.format("{%s, %s}", x, y);
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
