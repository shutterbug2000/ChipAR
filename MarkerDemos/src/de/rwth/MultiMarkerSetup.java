package de.rwth;

import javax.microedition.khronos.opengles.GL10;

import com.shutterbug.chip8.chip.test;

import geo.GeoObj;
import gl.Color;
import gl.CustomGLSurfaceView;
import gl.GL1Renderer;
import gl.GLCamera;
import gl.GLFactory;
import gl.GLRenderer;
import gl.Renderable;
import gl.scenegraph.MeshComponent;
import gl.scenegraph.Shape;
import gui.GuiSetup;
import markerDetection.MarkerDetectionSetup;
import markerDetection.MarkerObjectMap;
import markerDetection.UnrecognizedMarkerListener;
import system.EventManager;
import util.Vec;
import worldData.Obj;
import worldData.RenderableEntity;
import worldData.SystemUpdater;
import worldData.Updateable;
import worldData.Visitor;
import worldData.World;
import actions.Action;
import actions.ActionBufferedCameraAR;
import actions.ActionMoveCameraBuffered;
import actions.ActionRotateCameraBuffered;
import android.app.Activity;
import android.util.Log;
import commands.Command;

public class MultiMarkerSetup extends MarkerDetectionSetup {

	public static GLCamera camera;
	private World world;
	public static MeshComponent mesh1;
	private MeshComponent mesh2;
	private test chip = new test();
	private byte[] display;
	private int displen;
	private chipRender cr;
	public static MarkerObjectMap markerObjectMap;

	@Override
	public void _a_initFieldsIfNecessary() {
		chip.init();
		chip.loadProgram("/storage/emulated/0/Download/pong2.c8");
		display = chip.display;
		displen = display.length;
		camera = new GLCamera(new Vec(0, 0, 10));
		Log.d("Look",camera.getPositionOnGroundWhereTheCameraIsLookingAt().toString());
		camera.setRotation(0, 90, 270);
		world = new World(camera);
		mesh1 = new Shape();

		/*
		mesh1.addChild(GLFactory.getInstance().newCoordinateSystem());
		mesh1.setColor(Color.blue());
		// mesh.add(GLFactory.getInstance().newCircle(new Color(0, 0, 1,
		// 0.6f)));
		mesh1.addChild(GLFactory.getInstance().newCube());
		MeshComponent cube = GLFactory.getInstance().newCube();
		//cube.setScale(new Vec(-10,-10,-10));
		cube.setColor(Color.green());//(new Vec(-300,0,0));
		mesh1.addChild(cube);
		mesh1.addChild(GLFactory.getInstance().newCube());
		MeshComponent cube2 = GLFactory.getInstance().newCube();
		cube2.setColor(Color.red());
		cube2.setPosition((new Vec(-2,0,0)));
		mesh1.addChild(cube2);
		//mesh2.setColor(Color.red());
		//mesh2.setRotation(new Vec(0,2,0));
		mesh1.addChild(GLFactory.getInstance().newCoordinateSystem());
		mesh1.addChild(GLFactory.getInstance().newCircle(
				new Color(0, 0, 1, 0.6f)));
*/
		
		mesh2 = new Shape();
		/*
		mesh2.addChild(GLFactory.getInstance().newCube());
		//mesh2.setColor(Color.red());
		//mesh2.setRotation(new Vec(0,2,0));
		mesh2.addChild(GLFactory.getInstance().newCoordinateSystem());
		mesh2.addChild(GLFactory.getInstance().newCircle(
				new Color(0, 0, 1, 0.6f)));
		// mesh1.add(GLFactory.getInstance().newCube());
		 */
		cr = new chipRender(chip, display, displen, mesh1, world);
	}

	@Override
	public UnrecognizedMarkerListener _a2_getUnrecognizedMarkerListener() {
		return new UnrecognizedMarkerListener() {

			@Override
			public void onUnrecognizedMarkerDetected(int markerCode,
					float[] mat, int startIdx, int endIdx, int rotationValue) {
				System.out.println("unrecognized markerCode=" + markerCode);
			}
		};

	}

	@Override
	public void _a3_registerMarkerObjects(MarkerObjectMap markerObjectMap) {
//		this.markerObjectMap = markerObjectMap;
		markerObjectMap.put(new SimpleMeshPlacer(0, mesh1, camera));
		markerObjectMap.put(new SimpleMeshPlacer(1, mesh2, camera));

		/*
		 * example for more complex behavior:
		 */
		/*
		markerObjectMap.put(new BasicMarker(2, camera) {

			MeshComponent targetMesh;
			boolean firstTime = true;

			@Override
			public void setObjectPos(Vec positionVec) {
				/*
				 * the first time this method is called an object could be
				 * created and added to the world
				 */
		/*
				if (firstTime) {
					firstTime = false;
					Obj aNewObject = new Obj();
					targetMesh = GLFactory.getInstance().newArrow();
					aNewObject.setComp(targetMesh);
					world.add(aNewObject);
				}
				targetMesh.setPosition(positionVec);
			}
	

			@Override
			public void setObjRotation(float[] rotMatrix) {
				if (targetMesh != null)
					targetMesh.setRotationMatrix(rotMatrix);
			}
		});
		*/
	}

	@Override
	public void _b_addWorldsToRenderer(GL1Renderer renderer,
			GLFactory objectFactory, GeoObj currentPosition) {
		renderer.addRenderElement(world);
		Obj o = new Obj();
		o.setComp(mesh1);
		world.add(o);

		Obj o2 = new Obj();
		o2.setComp(mesh2);
		world.add(o2);

//		world.add(objectFactory.newHexGroupTest(new Vec()));

	}

	@Override
	public void _c_addActionsToEvents(EventManager eventManager,
			CustomGLSurfaceView arView, SystemUpdater updater) {
		arView.addOnTouchMoveListener(new ActionBufferedCameraAR(camera));
		Action rot = new ActionRotateCameraBuffered(camera);
		updater.addObjectToUpdateCycle(rot);
		eventManager.addOnOrientationChangedAction(rot);
		eventManager.addOnTrackballAction(new ActionMoveCameraBuffered(camera,
				1, 25));

	}

	@Override
	public void _d_addElementsToUpdateThread(SystemUpdater updater) {
		updater.addObjectToUpdateCycle(world);
		updater.addObjectToUpdateCycle(cr);
//		chip.run();
//		world.clear();
//		for(int i = 0; i < displen; i++) {
//			if(display[i] == 0){
//				/**
//				int x = (i % 64);
//				int y = (int)Math.floor(i / 64);
//				batch.draw(b2, (x * 10), (y * 10), 10, 10);
//				
//				*/
//			} else{
//				int x = (i % 64);
//				int y = (int)Math.floor(i / 64);
//				MeshComponent chipcube = GLFactory.getInstance().newCube();
//
//		chipcube.setPosition(new Vec(x,y,0));
//				mesh2.addChild(chipcube);
//				batch.begin();
//				batch.draw(w2, (x * 10), (y * 10), 10, 10);
//				time = (float)(time + .0001);
//				shader.setUniformf("time", time);
//				batch.end();
			}
//			}
//	}
	@Override
	public void _e2_addElementsToGuiSetup(GuiSetup guiSetup, Activity activity) {
		guiSetup.addButtonToBottomView(new Command() {

			@Override
			public boolean execute() {

				Vec rayPosition = new Vec();
				Vec rayDirection = new Vec();
				camera.getPickingRay(rayPosition, rayDirection,
						GLRenderer.halfWidth, GLRenderer.halfHeight);

				System.out.println("rayPosition=" + rayPosition);
				System.out.println("rayDirection=" + rayDirection);

				rayDirection.setLength(5);

				mesh1.setPosition(rayPosition.add(rayDirection));

				return false;
			}
		}, "Place 2 meters infront");

	}
}

class chipRender implements Updateable{
	
	private test chip;
	private byte[] display;
	private int displen;
	private MeshComponent mesh2;
	private World world;
	private MeshComponent[] cubes;
	private int i = 0;
	Vec pos = new Vec(0,0,0);

	public chipRender(test chip, byte[] display, int displen, MeshComponent mesh2, World world){
		this.chip = chip;
		this.display = display;
		this.displen = displen;
		this.mesh2 = mesh2;
		this.world = world;
		cubes = new MeshComponent[4096];
		for(int i = 0; i < 4096; i++){
			cubes[i] = GLFactory.getInstance().newCube();
		}
	}
	
	@Override
	public boolean update(float arg0, Updateable arg1) {
//		Log.d("hi", "LOL");
		chip.run();
		//if(i % 5 == 1)
		//MultiMarkerSetup.mesh1.clearChildren();
		for(int i = 0; i < displen; i++) {
			if(display[i] == 0){
				/*
				int x = (i % 64);
				int y = (int)Math.floor(i / 64);
				batch.draw(b2, (x * 10), (y * 10), 10, 10);
				
			*/
				pos.setTo(0, 0, -1000);
				cubes[i].setPosition(pos);
			} else{
				int x = (i % 64);
				int y = (int)Math.floor(i / 64);
				pos.setTo(x,y,-200);
				cubes[i].setPosition(pos);
				cubes[i].setColor(Color.red());
				MultiMarkerSetup.mesh1.remove(cubes[i]);
				MultiMarkerSetup.mesh1.addChild(cubes[i]);
//				MultiMarkerSetup.markerObjectMap.put(new SimpleMeshPlacer(0, MultiMarkerSetup.mesh1, MultiMarkerSetup.camera));
//				batch.begin();
//				batch.draw(w2, (x * 10), (y * 10), 10, 10);
//				time = (float)(time + .0001);
//				shader.setUniformf("time", time);
//				batch.end();
			}
			i++;
			}

		return true;
	}

}
