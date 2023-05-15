/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
 */
package cientistavuador.bakedlightingexperiment;

import cientistavuador.bakedlightingexperiment.camera.FreeCamera;
import cientistavuador.bakedlightingexperiment.ubo.CameraUBO;
import cientistavuador.bakedlightingexperiment.ubo.UBOBindingPoints;
import cientistavuador.bakedlightingexperiment.debug.AabRender;
import cientistavuador.bakedlightingexperiment.debug.Triangle;
import cientistavuador.bakedlightingexperiment.text.GLFontRenderer;
import cientistavuador.bakedlightingexperiment.text.GLFontSpecification;
import cientistavuador.bakedlightingexperiment.text.GLFontSpecifications;
import org.joml.Matrix4f;

/**
 *
 * @author Cien
 */
public class Game {

    private static final Game GAME = new Game();

    public static Game get() {
        return GAME;
    }

    private final FreeCamera camera = new FreeCamera();
    private final Triangle triangle = new Triangle();

    private Game() {

    }

    public void start() {
        camera.setPosition(0, 0, 3);
        camera.setUBO(CameraUBO.create(UBOBindingPoints.PLAYER_CAMERA));
    }

    public void loop() {
        camera.updateMovement();

        triangle.render(new Matrix4f(this.camera.getProjectionView()));
        AabRender.renderQueue(camera);

        GLFontRenderer.render(-1f, 0.90f,
                new GLFontSpecification[]{
                    GLFontSpecifications.OPENSANS_ITALIC_0_10_BANANA_YELLOW,
                    GLFontSpecifications.ROBOTO_THIN_0_05_WHITE
                },
                new String[]{
                    "BakedLightingExperiment\n",
                    new StringBuilder()
                            .append("FPS: ").append(Main.FPS).append('\n')
                            .append("X: ").append(format(camera.getPosition().x())).append(" ")
                            .append("Y: ").append(format(camera.getPosition().y())).append(" ")
                            .append("Z: ").append(format(camera.getPosition().z())).append('\n')
                            .append("Controls:\n")
                            .append("\tWASD + Space + Mouse - Move\n")
                            .append("\tShift - Run\n")
                            .append("\tAlt - Wander\n")
                            .append("\tCtrl - Unlock/Lock mouse\n")
                            .toString()
                }
        );

        Main.WINDOW_TITLE += " (DrawCalls: " + Main.NUMBER_OF_DRAWCALLS + ", Vertices: " + Main.NUMBER_OF_VERTICES + ")";
        Main.WINDOW_TITLE += " (x:" + (int) Math.floor(camera.getPosition().x()) + ",y:" + (int) Math.floor(camera.getPosition().y()) + ",z:" + (int) Math.ceil(camera.getPosition().z()) + ")";
    }

    private String format(double d) {
        return String.format("%.2f", d);
    }

    public void mouseCursorMoved(double x, double y) {
        camera.mouseCursorMoved(x, y);
    }

    public void windowSizeChanged(int width, int height) {
        camera.setDimensions(width, height);
    }

    public void keyCallback(long window, int key, int scancode, int action, int mods) {

    }

    public void mouseCallback(long window, int button, int action, int mods) {

    }
}
