var JavaPackages = new JavaImporter(
 Packages.ray.rage.scene.SceneManager,
 Packages.ray.rage.scene.Light,
 Packages.ray.rage.scene.Light.Type,
 Packages.ray.rage.scene.Light.Type.POINT,
 Packages.java.awt.Color
 );
// creates a RAGE object - in this case a light
with (JavaPackages)
{ 
if (toggleLights==0){
sm.getAmbientLight().setIntensity(new Color(.2, .2, .2));
var plight = sm.getLight("testLamp3");
plight.setAmbient(new Color(.3, .3, .3));
plight.setDiffuse(new Color(.7, .7, .7));
plight.setSpecular(new Color(1.0, 1.0, 1.0));
plight.setRange(5);
print("turned on lights");
} else {
	
sm.getAmbientLight().setIntensity(new Color(.05, .05, .05));
var plight = sm.getLight("testLamp3");
plight.setAmbient(new Color(.0, .0, .0));
plight.setDiffuse(new Color(.0, .0, .0));
plight.setSpecular(new Color(0.0, 0.0, 0.0));
plight.setRange(5);
print("turned off lights");
}
}