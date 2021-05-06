package a3;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import myGameEngine.*;
import ray.rage.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.input.*;
import ray.input.action.*;
import ray.rage.util.BufferUtil;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import ray.rage.asset.texture.Texture;
import ray.rage.rendersystem.shader.*;
import java.util.Random;
import java.util.ArrayList;
import net.java.games.input.Controller;
import ray.rage.asset.material.Material;
import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.rage.util.*;
import java.awt.geom.*;
import java.util.UUID;
import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;
import ray.networking.IGameConnection.ProtocolType;
import ray.networking.*;
import java.net.InetAddress;
import ray.networking.server.UDPClientInfo;
import java.util.*;
import java.net.UnknownHostException;
import ray.input.action.AbstractInputAction;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.Invocable;
import java.util.List;

import ray.physics.PhysicsEngine;
import ray.physics.PhysicsObject;
import ray.physics.PhysicsEngineFactory;
import static ray.rage.scene.SkeletalEntity.EndType.*;


public class MyGame extends VariableFrameRateGame {
	private Action moveYawController, movePitchDown,moveYawRight,moveYawLeft,
	movePitchUp,moveUpAction ,moveForwardAction,moveBackwardAction,
	moveRightAction,moveLeftAction,quitGameAction;
	public SceneNode avatarN,avatar2N,cloudN,camelN,palmN,rockN,fireN;
	private Camera camera;
	private SceneManager sm;
	private InputManager im;
	private static final String SKYBOX_NAME = "SkyBox";
	private boolean skyBoxVisible = true;
	GL4RenderSystem rs;
	float elapsTime = 0.0f;
	int elapsTimeSec;
	private Camera3Pcontroller orbitController1;
	private String serverAddress;
	private int serverPort,toggleLights=0;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected;
	private Vector<UUID> gameObjectsToRemove;
	private SceneNode tessN;
	public int ghostCounter=0,palmCounter=1,rockCounter=1;
	

	private ScriptEngine jsEngine;
	private ScriptEngine engine;
	protected ColorAction colorAction;
	protected File scriptFile3,scriptFile;
	private String sName = "a3/CreateLights.js";
	private SceneNode ball1Node,gndNode,groundNode,ball2Node;

	private final static String GROUND_E = "Ground";
	private final static String GROUND_N = "GroundNode";
	private PhysicsEngine physicsEng;
	private PhysicsObject ball1PhysObj, ball2PhysObj, gndPlaneP,rockPhysObj,avatarPhyObj;
	private boolean running = false;
	private Entity ball1Entity;
	private int potiID, health, round;

    public MyGame(String serverAddr, int sPort) {
        super();
		this.serverAddress = serverAddr;
		this.serverPort = sPort;
		this.serverProtocol = ProtocolType.UDP;
		health = 60;
		round = 1;


    }

	 public static void main(String[] args) {
        Game game = new MyGame(args[0], Integer.parseInt(args[1]));
        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }

	private void setupNetworking(){
		gameObjectsToRemove = new Vector<UUID>();
		isClientConnected = false;
		try{
			protClient = new ProtocolClient(InetAddress.
			getByName(serverAddress), serverPort, serverProtocol, this);
		}catch (UnknownHostException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		if (protClient == null){
			System.out.println("missing protocol host");
		}else{
			// ask client protocol to send initial join message
			//to server, with a unique identifier for this client
			protClient.sendJoinMessage();
		}
	}


	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
	}

	@Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
		/* sceneManager=sm;
        SceneNode rootNode = sm.getRootSceneNode();
        camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera);


		camera.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
		camera.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
		camera.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));

		camera.setPo((Vector3f)Vector3f.createFrom(0.0f, 0.0f, 0.0f));

        SceneNode cameraNode = rootNode.createChildSceneNode(camera.getName() + "Node");
        cameraNode.attachObject(camera);
		camera.setMode('n');
		camera.getFrustum().setFarClipDistance(1000.0f); */
		this.sm = sm;

		SceneNode rootNode = sm.getRootSceneNode();
		Camera camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
		rw.getViewport(0).setCamera(camera);
		SceneNode cameraN =
		rootNode.createChildSceneNode("MainCameraNode");
		cameraN.attachObject(camera);
		camera.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
		camera.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
		camera.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));
		camera.setMode('n');
		camera.getFrustum().setFarClipDistance(1000.0f);


    }

	protected void setupOrbitCamera(Engine eng, SceneManager sm){

		SceneNode avatarN = sm.getSceneNode("myAvatarNode");
		SceneNode cameraN = sm.getSceneNode("MainCameraNode");
		Camera camera = sm.getCamera("MainCamera");
		String kbName = im.getKeyboardName();
		orbitController1 = new Camera3Pcontroller(camera, cameraN, avatarN, kbName, im);
	}

	@Override
	protected void setupScene(Engine eng, SceneManager sm) throws IOException {
		 im = new GenericInputManager();
		this.sm=sm;
		sm=this.sm;

		// Ball 1
		ball1Entity = sm.createEntity("ball1", "Poti.obj");
		ball1Node = sm.getRootSceneNode().createChildSceneNode("Ball1Node");
		ball1Node.attachObject(ball1Entity);
		ball1Node.setLocalScale(.007f,.007f,.007f);
		ball1Node.setLocalPosition(9.19f, 2.5f, -5.61006f);
		/* 
		// Ball 2
		Entity ball2Entity = sm.createEntity("ball2", "Poti.obj");
		ball2Node = sm.getRootSceneNode().createChildSceneNode("Ball2Node");
		ball2Node.attachObject(ball2Entity);
		ball2Node.setLocalScale(.007f,.007f,.007f);
		ball2Node.setLocalPosition(2.19f, 0f, -5.61006f); */
		
		

		// Ground plane
		Entity groundEntity = sm.createEntity(GROUND_E, "cube.obj");
		groundNode = sm.getRootSceneNode().createChildSceneNode(GROUND_N);
		groundNode.attachObject(groundEntity);
		groundNode.setLocalPosition(10.567f, -.2f, -6.1755f);



		 SkeletalEntity avatarE=sm.createSkeletalEntity("myAvatar","avatarTest.rkm","avatar1.rks");
		 Texture tex = sm.getTextureManager().getAssetByPath("avatar1.jpeg");
		 TextureState tstate = (TextureState)sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		 tstate.setTexture(tex);
		 avatarE.setRenderState(tstate);
		 //avatarE.setPrimitive(Primitive.TRIANGLES);
		 avatarN= sm.getRootSceneNode().createChildSceneNode(avatarE.getName()+"Node");
		 avatarN.attachObject(avatarE);
		 //avatarN.attachObject(camera);
		 avatarN.setLocalScale(.1f,.1f,.1f);
		 avatarN.setLocalPosition(-6.99f,0.0f,-3.18f);
		 
		 avatarE.loadAnimation("bow1Animation", "bow1.rka");
		 
		 

		 createCloud(eng,sm);
		 createCamel(eng,sm);
		 createRock(eng,sm,38.558334f,0.8946079f,-14.2430105f,31.503038f,0.8827f,-14.5056521f);
		 createRock(eng,sm,4.921572f,0.8913952f,39.10878f,13.858516f,0.89139f,37.27713f);
		 createRock(eng,sm,-40.21158f,0.8823534f,8.47265247f,-35.221796f,0.8946079f,6.99243646f);
		 createFire(eng,sm);

		 for (int i=0;i<4;i++){
			 createPalm(eng,sm,palmCounter);
			 palmCounter++;
		 }
		 setupNetworking();

		


		 // set up lights
        sm.getAmbientLight().setIntensity(new Color(.05f, .05f, .05f));
        Light plight = sm.createLight("testLamp1", Light.Type.POINT);
        plight.setAmbient(new Color(.05f, .05f, .05f));
        plight.setDiffuse(new Color(0.3f, 0.3f, 0.3f));
        plight.setSpecular(new Color(0.50f, 0.50f, 0.50f));
        plight.setRange(50f);
        SceneNode plightNode =
        sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);
        plightNode.setLocalPosition(1.0f, 1.0f, 5.0f);

		Light plight2 = sm.createLight("testLamp3", Light.Type.POINT);
		plight2.setAmbient(new Color(.0f, .0f, .0f));
		plight2.setDiffuse(new Color(.0f, .0f, .0f));
		plight2.setSpecular(new Color(0.0f, 0.0f, 0.0f));
		plight2.setRange(5);
        SceneNode plightNode2 =
        sm.getRootSceneNode().createChildSceneNode("plightNode2");
        plightNode2.attachObject(plight2);



		// set up sky box
		Configuration conf = eng.getConfiguration();
		TextureManager tm = getEngine().getTextureManager();
		tm.setBaseDirectoryPath(conf.valueOf("assets.skyboxes.path"));
		Texture front = tm.getAssetByPath("front.jpg");
		Texture back = tm.getAssetByPath("back.jpg");
		Texture left = tm.getAssetByPath("left.jpg");
		Texture right = tm.getAssetByPath("right.jpg");
		Texture top = tm.getAssetByPath("top.jpg");
		Texture bottom = tm.getAssetByPath("bottom.jpg");
		 tm.setBaseDirectoryPath(conf.valueOf("assets.textures.path"));
		// cubemap textures are flipped upside-down.
		// All textures must have the same dimensions, so any image’s
		// heights will work since they are all the same height
		AffineTransform xform = new AffineTransform();
		xform.translate(0, front.getImage().getHeight());
		xform.scale(1d, -1d);
		front.transform(xform);
		back.transform(xform);
		left.transform(xform);
		right.transform(xform);
		top.transform(xform);
		bottom.transform(xform);
		SkyBox sb = sm.createSkyBox(SKYBOX_NAME);
		sb.setTexture(front, SkyBox.Face.FRONT);
		sb.setTexture(back, SkyBox.Face.BACK);
		sb.setTexture(left, SkyBox.Face.LEFT);
		sb.setTexture(right, SkyBox.Face.RIGHT);
		sb.setTexture(top, SkyBox.Face.TOP);
		sb.setTexture(bottom, SkyBox.Face.BOTTOM);
		sm.setActiveSkyBox(sb);



		Tessellation tessE = sm.createTessellation("tessE", 6);
		tessE.setSubdivisions(8f);
		tessN =sm.getRootSceneNode().createChildSceneNode("TessN");
		tessN.attachObject(tessE);
		tessN.scale(100, 200, 100);
		tessE.setHeightMap(this.getEngine(), "ter.jpg");
		tessE.setTexture(this.getEngine(), "bottom.jpg");





		ScriptEngineManager factory = new ScriptEngineManager();
		java.util.List<ScriptEngineFactory> list = factory.getEngineFactories();
		jsEngine = factory.getEngineByName("js");

		scriptFile3 = new File("UpdateLightColor.js");
		this.runScript(scriptFile3, jsEngine);
		/* im = new GenericInputManager();
		String kbName = im.getKeyboardName();
		colorAction = new ColorAction(sm, jsEngine, scriptFile3);
		im.associateAction(kbName,
		net.java.games.input.Component.Identifier.Key.SPACE,
		colorAction,
		InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY); */

		initPhysicsSystem();
		createRagePhysicsWorld();

		setupOrbitCamera(eng,sm);
		setupInputs(sm);
		avatarN.yaw(Degreef.createFrom(45.f));
	}

	private void doBow1() {
		SkeletalEntity avatarSE = (SkeletalEntity)getEngine().getSceneManager().getEntity("myAvatar");
		avatarSE.stopAnimation();
		avatarSE.playAnimation("bow1Animation", 0.5f, LOOP, 0);
	}
	private void runScript(File scriptFile3, ScriptEngine engine){
		try{
			FileReader fileReader = new FileReader(scriptFile3);
			engine.eval(fileReader);
			fileReader.close();
		}catch (FileNotFoundException e1){
			System.out.println(scriptFile3 + " not found " + e1);
		}catch (IOException e2){
			System.out.println("IO problem with " + scriptFile3 + e2);
		}catch (ScriptException e3){
			System.out.println("Script Exception in " + scriptFile3 + e3);
		}catch (NullPointerException e4){
			System.out.println ("Null ptr exception reading " + scriptFile3 + e4);
		}
	}


	public void createCamel(Engine eng, SceneManager sm) throws IOException{
		Entity camelE=sm.createEntity("myCamel","camel.obj");
		 camelE.setPrimitive(Primitive.TRIANGLES);
		 camelN= sm.getRootSceneNode().createChildSceneNode(camelE.getName()+"Node");
		 camelN.attachObject(camelE);
		 camelN.setLocalScale(.6f,.6f,.6f);
		 camelN.moveForward(5.0f);



	}
	public void createPalm(Engine eng,SceneManager sm, int num) throws IOException{

		Random r = new Random();
		Entity palmE=sm.createEntity("myPalm"+num,"palm.obj");
		palmE.setPrimitive(Primitive.TRIANGLES);
		palmN= sm.getRootSceneNode().createChildSceneNode(palmE.getName()+"Node");
		palmN.attachObject(palmE);
		palmN.setLocalScale(.1f,.1f,.1f);
		palmN.setLocalPosition(((float)(r.nextInt(40)-20)), 0f, ((float)(r.nextInt(40)-20)));

	}


	public void createRock(Engine eng, SceneManager sm, float x, float y, float z,float lookX, float lookY, float lookZ)throws IOException{
		Entity rockE=sm.createEntity("myRock"+rockCounter,"rock.obj");
		rockE.setPrimitive(Primitive.TRIANGLES);
		rockN= sm.getRootSceneNode().createChildSceneNode(rockE.getName()+rockCounter+"Node");
		rockN.attachObject(rockE);
		rockN.setLocalScale(.01f,.01f,.01f);
		rockN.setLocalPosition(x,y,z);
		rockN.lookAt(lookX,lookY,lookZ);
		rockCounter++;
		//have to figure out how to rotate it 90 degrees with either rotate() or something.
		//rockN.pitch((angle)90);
	}
	public void createFire(Engine eng, SceneManager sm)throws IOException{
		Entity fireE=sm.createEntity("myFire","fire.obj");
		fireE.setPrimitive(Primitive.TRIANGLES);
		fireN= sm.getRootSceneNode().createChildSceneNode(fireE.getName()+"Node");
		fireN.attachObject(fireE);
		fireN.setLocalScale(1f,1f,1f);
		fireN.moveForward(3.0f);

	}


	public void createCloud(Engine eng, SceneManager sm)throws IOException{
		Entity cloudE=sm.createEntity("myCloud","avatar3.obj");
		cloudE.setPrimitive(Primitive.TRIANGLES);
		cloudN= sm.getRootSceneNode().createChildSceneNode(cloudE.getName()+"Node");
		cloudN.attachObject(cloudE);
		cloudN.setLocalScale(.3f,.3f,.3f);
		cloudN.setLocalPosition(10.567f, 0.0f, -6.1755f);


	}

	private void initPhysicsSystem(){
		String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = {0, -3f, 0};
		physicsEng = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEng.initSystem();
		physicsEng.setGravity(gravity);
	}
	private void createRagePhysicsWorld(){
		float mass = 1.0f;
		float up[] = {0,1,0};
		double[] temptf;
		float[] tempsize = {7f,3.5f,3f};
		float[] avatarSize={1f,2.5f,1.0f};
		
		temptf = toDoubleArray(rockN.getLocalTransform().toFloatArray());
		rockPhysObj = physicsEng.addBoxObject(physicsEng.nextUID(),60f,temptf,tempsize);
		rockPhysObj.setFriction(10f);
		rockPhysObj.setBounciness(0f);
		rockN.setPhysicsObject(rockPhysObj);

		temptf = toDoubleArray(avatarN.getLocalTransform().toFloatArray());
		avatarPhyObj = physicsEng.addBoxObject(physicsEng.nextUID(),mass,temptf,avatarSize);
		avatarPhyObj.setFriction(50f);

		avatarPhyObj.setBounciness(1.0f);
		avatarN.setPhysicsObject(avatarPhyObj);

			//ball1
		temptf = toDoubleArray(ball1Node.getLocalTransform().toFloatArray());
		potiID = physicsEng.nextUID();
		ball1PhysObj = physicsEng.addSphereObject(potiID,
		mass, temptf, .6f);

		
		ball1PhysObj.setBounciness(0f);
		ball1Node.setPhysicsObject(ball1PhysObj);

		/* //ball2
		temptf = toDoubleArray(ball2Node.getLocalTransform().toFloatArray());
		ball2PhysObj = physicsEng.addSphereObject(physicsEng.nextUID(),
		mass, temptf, .6f);

		ball2PhysObj.setBounciness(0f);
		ball2Node.setPhysicsObject(ball2PhysObj); */
	

		temptf = toDoubleArray(groundNode.getLocalTransform().toFloatArray());
		gndPlaneP = physicsEng.addStaticPlaneObject(physicsEng.nextUID(),
		temptf, up, 0.0f);

		gndPlaneP.setBounciness(1.0f);
		groundNode.scale(3f, .05f, 3f);
		groundNode.setLocalPosition(10.567f, -.2f, -6.1755f);
		groundNode.setPhysicsObject(gndPlaneP);
		// can also set damping, friction, etc.
}

	public class ColorAction extends AbstractInputAction{
		private SceneManager sm;
		private ScriptEngine jsEngine;
		private File scriptFile3;
		public ColorAction(SceneManager s, ScriptEngine j, File file) {
			sm = s;
			jsEngine = j;
			scriptFile3 = file;

		} // constructor

		public void performAction(float time, net.java.games.input.Event e){
			//cast the engine so it supports invoking functions
			Invocable invocableEngine = (Invocable) jsEngine ;
			//get the light to be updated
			Light lgt = sm.getLight("testLamp1");
			// invoke the script function
			try{
				invocableEngine.invokeFunction("updateAmbientColor", lgt);
			}catch (ScriptException e1){
				System.out.println("ScriptException in " + scriptFile3 + e1);
			}catch (NoSuchMethodException e2){
				System.out.println("No such method in " + scriptFile3 + e2);
			}catch (NullPointerException e3){
				System.out.println ("Null ptr exception reading " + scriptFile3 + e3);
			}
		}
	}

	protected void setupInputs(SceneManager sm){

		SceneNode avatarN = getEngine().
			getSceneManager().getSceneNode("myAvatarNode");
		String kbName = im.getKeyboardName();
		String gpName = im.getFirstGamepadName();

		// build some action objects for doing things in response to user input

		//moveforward i accidentally deleted the protclient part of method
		MoveForwardAction moveForwardAction = new MoveForwardAction(avatarN,this,protClient);
		MoveBackwardAction moveBackwardAction = new MoveBackwardAction(avatarN,this);
		MoveLeftAction moveLeftAction = new MoveLeftAction(avatarN,this);
		MoveRightAction moveRightAction= new MoveRightAction(avatarN,this);
		LookUpAction lookUpAction = new LookUpAction(orbitController1);
		LookDownAction lookDownAction = new LookDownAction(orbitController1);
		OrbitLeftAction orbitLeftAction = new OrbitLeftAction(orbitController1);
		OrbitRightAction orbitRightAction = new OrbitRightAction(orbitController1);
		MoveYawLeftAction yawLeft = new MoveYawLeftAction(avatarN,orbitController1);
		RotateLeftAction rotateLeftAction = new RotateLeftAction(avatarN, orbitController1);
		RotateRightAction rotateRightAction = new RotateRightAction(avatarN, orbitController1);
		
		quitGameAction = new QuitGameAction(this);


		colorAction = new ColorAction(sm, jsEngine, scriptFile3);



	ArrayList controllers = im.getControllers();
	for (int i =0;i<controllers.size();i++){
		Controller c = (Controller)controllers.get(i);
		if (c.getType()==Controller.Type.KEYBOARD){

			im.associateAction(c,net.java.games.input.Component.Identifier.Key.ESCAPE,quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(c,net.java.games.input.Component.Identifier.Key.W,moveForwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(c,net.java.games.input.Component.Identifier.Key.A,moveLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(c,net.java.games.input.Component.Identifier.Key.S,moveBackwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(c,net.java.games.input.Component.Identifier.Key.D,moveRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

			im.associateAction(c,net.java.games.input.Component.Identifier.Key.UP,lookUpAction,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(c,net.java.games.input.Component.Identifier.Key.DOWN,lookDownAction,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

			//im.associateAction(c,net.java.games.input.Component.Identifier.Key.LEFT,orbitLeftAction,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			//im.associateAction(c,net.java.games.input.Component.Identifier.Key.RIGHT,orbitRightAction,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

			im.associateAction(c,net.java.games.input.Component.Identifier.Key.LEFT,rotateLeftAction,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(c,net.java.games.input.Component.Identifier.Key.RIGHT,rotateRightAction,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

			im.associateAction(c,net.java.games.input.Component.Identifier.Key.Q,orbitRightAction,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(c,
		net.java.games.input.Component.Identifier.Key.SPACE,
		colorAction,
		InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		}
	}


	}




	 @Override
    protected void update(Engine engine) {
		// build and set HUD
		/* rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		im.update(elapsTime);
		 */
		
		
				// build and set HUD
		rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		//elapsTimeStr = Integer.toString(elapsTimeSec);
		//counterStr = Integer.toString(counter);
		//dispStr = "Time = " + elapsTimeStr +"  Score= "+score;

		rs.setHUD("Health: " + health + "          Round: " + round, 15, 15);
		im.update(elapsTime);
		orbitController1.updateCameraPosition();
		processNetworking(elapsTime);
		

		float time = engine.getElapsedTimeMillis();
		if (running){
			Matrix4 mat;
			physicsEng.update(time);

			for (SceneNode s : engine.getSceneManager().getSceneNodes()){
				if (s.getPhysicsObject() != null && !s.getName().equals("myAvatarNode") /*&& !s.getName().equals("myRockNode")*/){
					mat = Matrix4f.createFrom(toFloatArray(s.getPhysicsObject().getTransform()));
					s.setLocalPosition(mat.value(0,3),mat.value(1,3),mat.value(2,3));
					//System.out.println(mat);
					
					}
					

				if (s.getPhysicsObject()!= null && s.getName().equals("myAvatarNode")){
					Matrix4 avatarLoc=s.getLocalTransform();
					float[] temp = avatarLoc.toFloatArray();
					double[] locArr = toDoubleArray(temp);
					//System.out.println(locArr);
					avatarPhyObj.setTransform(locArr);
					
					//create a matrix of new location or the transform
					//set the transform to the physics objects
					
					
				}

			}
		}
		//update animation
		SkeletalEntity avatarSE = (SkeletalEntity)engine.getSceneManager().getEntity("myAvatar");
		avatarSE.update();
	}



 

	private float[] toFloatArray(double[] arr){
		if (arr == null) return null;
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++){
			ret[i] = (float)arr[i];
		}
		return ret;
		}


	private double[] toDoubleArray(float[] arr){
		if (arr == null) return null;
		int n = arr.length;
		double[] ret = new double[n];

		for (int i = 0; i < n; i++){
			ret[i] = (double)arr[i];
		}
		return ret;
	}




	protected void processNetworking(float elapsTime){
		// Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
		// remove ghost avatars for players who have left the game
		/* Iterator<UUID> it = gameObjectsToRemove.iterator();
		while(it.hasNext()){
			sm.destroySceneNode(it.next().toString());
		} */
		//gameObjectsToRemove.clear();
	}
	public Vector3 getPlayerPosition(){
		//avatarN = sm.getSceneNode("myAvatarNode");
		return avatarN.getWorldPosition();
	}
	public void addGhostAvatarToGameWorld(GhostAvatar avatar, Vector3 position) throws IOException{
		if (avatar != null){
			/* Entity ghostE = sm.createEntity("ghost", "earth.obj");
			ghostE.setPrimitive(Primitive.TRIANGLES);
			SceneNode ghostN = sm.getRootSceneNode().
			createChildSceneNode(avatar.getID().toString());
			ghostN.attachObject(ghostE);
			ghostN.setLocalPosition(1.0f,1.0f,1.0f);
			avatar.setNode(ghostN);
			avatar.setEntity(ghostE); */
			//avatar.setPosition(0.0f,0.0f,0.0f);

		Entity avatar2E=sm.createEntity("myAvatar"+ghostCounter,"avatar3.obj");
		 avatar2E.setPrimitive(Primitive.TRIANGLES);
		 avatar2N= sm.getRootSceneNode().createChildSceneNode(avatar2E.getName()+"Node");
		 avatar2N.attachObject(avatar2E);
		 //avatarN.attachObject(camera);
		 avatar2N.setLocalScale(.1f,.1f,.1f);
		 avatar2N.setLocalPosition(position);
		 System.out.println(ghostCounter);
		 ghostCounter++;
		}
	}

	public void removeGhostAvatarFromGameWorld(GhostAvatar avatar){
		if(avatar != null) gameObjectsToRemove.add(avatar.getID());
	}
	private abstract class SendCloseConnectionPacketAction extends AbstractInputAction {
		// for leaving the game... need to attach to an input device
		//@Override
		public void performAction(float time, Event evt){
			if(protClient != null && isClientConnected == true){
				protClient.sendByeMessage();
			}
		}
	}
	public void addLightsWithScripts(){
	ScriptEngineManager factory = new ScriptEngineManager();
      List<ScriptEngineFactory> list = factory.getEngineFactories();
      scriptFile = new File(sName);
	  jsEngine.put("toggleLights",toggleLights);
	  jsEngine.put("sm", sm);
      this.runScript(scriptFile,jsEngine);
	  if (toggleLights==0){
		  toggleLights=1;
	  } else {
		  toggleLights=0;
	  }
	//SceneNode plightNode2 =sm.getRootSceneNode().createChildSceneNode("plightNode2");
	//plightNode2.attachObject((Light)jsEngine.get("plight"));

		}
	public void killLightsWithScripts(){
		ScriptEngineManager factory = new ScriptEngineManager();
      List<ScriptEngineFactory> list = factory.getEngineFactories();
      scriptFile = new File("a3/KillLights.js");
	  jsEngine.put("sm", sm);
	  jsEngine.put("sm", sm);
      this.runScript(scriptFile,jsEngine);
	}


	@Override
    public void keyPressed(KeyEvent e) {
		SkeletalEntity avatarSE =(SkeletalEntity) getEngine().getSceneManager().getEntity("myAvatar");

       switch (e.getKeyCode()) {
            case KeyEvent.VK_C:
				addLightsWithScripts();
				break;
			case KeyEvent.VK_L:
				System.out.println(avatarN.getLocalPosition());
				//kill();
				break;
			case KeyEvent.VK_P:
				System.out.println("starting physics");
				running=true;
				break;
			case KeyEvent.VK_G:	
				if (getDistance(ball1Node,avatarN)<1){
					System.out.println("close enough to grab");
					try{
						physicsEng.removeObject(potiID);
						ball1Node.detachObject(ball1Entity);
						health = 100;
						doBow1();
					}catch(Exception eu){}
					
					
					
				} else {
					System.out.println("not close enough to grab");
				}	
					
				
				
				
				
				
				break;
			case KeyEvent.VK_N:
				ball1Node.detachObject(ball1Entity);
				
				break;
			case KeyEvent.VK_I:
				System.out.println("incremented round");
				round++;
				System.out.println("Round = " + round);
				break;

        }
        super.keyPressed(e);
    }
	
	public float getDistance(SceneNode node1, SceneNode node2){
		float node1X = node1.getLocalPosition().x();
		float node1Y = node1.getLocalPosition().y();
		float node1Z = node1.getLocalPosition().z();
		float node2X = node2.getLocalPosition().x();
		float node2Y = node2.getLocalPosition().y();
		float node2Z = node2.getLocalPosition().z();
		
		return (float) (Math.sqrt((double)+(node1X-node2X)*(node1X-node2X)+(node1Y-node2Y)*(node1Y-node2Y)+(node1Z-node2Z)*(node1Z-node2Z)));
		
	}

	public void updateVerticalPosition(){
		SceneNode avatarN =this.getEngine().getSceneManager().
		getSceneNode("myAvatarNode");
		SceneNode tessN =this.getEngine().getSceneManager().
		getSceneNode("TessN");
		Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));

		// Figure out Avatar's position relative to plane
		Vector3 worldAvatarPosition = avatarN.getWorldPosition();
		Vector3 localAvatarPosition = avatarN.getLocalPosition();
	// use avatar World coordinates to get coordinates for height
		Vector3 newAvatarPosition = Vector3f.createFrom(localAvatarPosition.x(),tessE.getWorldHeight(worldAvatarPosition.x(),worldAvatarPosition.z()),localAvatarPosition.z());
	// use avatar Local coordinates to set position, including height
		avatarN.setLocalPosition(newAvatarPosition);
}
	public void setIsConnected(boolean bool) {
		isClientConnected = bool;
	}
	public int getRound() {
		return round;
	}
}
