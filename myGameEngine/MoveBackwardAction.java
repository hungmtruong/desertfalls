package myGameEngine;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;
import a3.MyGame;
public class MoveBackwardAction extends AbstractInputAction
{
	private Node avN;
	private MyGame myGame;
	public MoveBackwardAction(Node n, MyGame g)
	{ 
		myGame=g;
		avN = n;
	}
	public void performAction(float time, Event e)
	{ 
		avN.moveBackward(0.12f);
		myGame.updateVerticalPosition();
	}
}