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

import static cientistavuador.bakedlightingexperiment.Main.DEFAULT_CLEAR_COLOR;
import cientistavuador.bakedlightingexperiment.camera.FreeCamera;
import cientistavuador.bakedlightingexperiment.cube.Cube;
import cientistavuador.bakedlightingexperiment.cube.CubeProgram;
import cientistavuador.bakedlightingexperiment.cube.CubeVAO;
import cientistavuador.bakedlightingexperiment.cube.light.Light;
import cientistavuador.bakedlightingexperiment.cube.light.directional.DirectionalLight;
import cientistavuador.bakedlightingexperiment.ubo.CameraUBO;
import cientistavuador.bakedlightingexperiment.ubo.UBOBindingPoints;
import cientistavuador.bakedlightingexperiment.debug.AabRender;
import cientistavuador.bakedlightingexperiment.text.GLFontRenderer;
import cientistavuador.bakedlightingexperiment.text.GLFontSpecification;
import cientistavuador.bakedlightingexperiment.text.GLFontSpecifications;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3dc;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

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
    private final DirectionalLight sun = new DirectionalLight();
    private final List<Cube> cubes = new ArrayList<>();
    private final List<Light> lights = new ArrayList<>();
    private float[] colors = {1f, 1f, 1f};
    private int currentComponent = 0;
    private boolean textEnabled = true;

    private Game() {

    }

    public void start() {
        camera.setPosition(0, 1f, 25f);
        camera.setUBO(CameraUBO.create(UBOBindingPoints.PLAYER_CAMERA));

        Matrix4f model = new Matrix4f()
                .translate(0f, -0.5f, 0f)
                .scale(50f, 1f, 50f);
        cubes.add(new Cube(model, true));

        lights.add(sun);
    }

    public void loop() {
        camera.updateMovement();
        Matrix4f cameraProjectionView = new Matrix4f(this.camera.getProjectionView());

        glUseProgram(Cube.SHADER_PROGRAM);
        CubeProgram.sendPerFrameUniforms(Cube.CUBE_TEXTURE, cameraProjectionView);

        for (Cube c : cubes) {
            if (c.isGroundCube()) {
                glBindVertexArray(CubeVAO.GROUND_CUBE_VAO);
            } else {
                glBindVertexArray(Cube.VAO);
            }

            CubeProgram.sendPerDrawUniforms(c.getLightmap(), c.getModel());
            glDrawElements(GL_TRIANGLES, Cube.NUMBER_OF_INDICES, GL_UNSIGNED_INT, 0);

            Main.NUMBER_OF_DRAWCALLS++;
            Main.NUMBER_OF_VERTICES += Cube.NUMBER_OF_INDICES;

            glBindVertexArray(0);
        }

        glUseProgram(0);

        AabRender.renderQueue(camera);

        if (this.textEnabled) {
            String componentSelected = null;
            switch (currentComponent) {
                case 0 -> {
                    componentSelected = ">R<, G, B";
                }
                case 1 -> {
                    componentSelected = " R,>G<, B";
                }
                case 2 -> {
                    componentSelected = " R, G,>B<";
                }
            }

            GLFontRenderer.render(-1f, 0.90f,
                    new GLFontSpecification[]{
                        GLFontSpecifications.OPENSANS_ITALIC_0_10_BANANA_YELLOW,
                        GLFontSpecifications.ROBOTO_THIN_0_05_WHITE,
                        GLFontSpecifications.OPENSANS_ITALIC_0_10_BANANA_YELLOW,
                        GLFontSpecifications.ROBOTO_THIN_0_05_WHITE,
                        GLFontSpecifications.ROBOTO_THIN_0_05_WHITE.withColor(colors[0], colors[1], colors[2], 1.0f)
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
                                .append("\tF - Spawn Cube\n")
                                .append("\tR - Remove Last Cube\n")
                                .append("\tL - Update Lightmap\n")
                                .append("\tX - Enable/Disable Sun. [").append(sun.isEnabled() ? "Enabled" : "Disabled").append("]\n")
                                .append("\tShift + Left Click - Place Spotlight. [Not Implemented]\n")
                                .append("\tShift + Right Click - Place Point Light. [Not Implemented]\n")
                                .append("\tB - Remove Last Light.\n")
                                .append("\tT - Hide This Wall of Text.\n")
                                .toString(),
                        "\nLight Color\n",
                        "\tO - Next Component\n\tP - Increase/Decrease Component\n\t\t" + componentSelected + "\n\t\t[" + formatColor(this.colors[0]) + "] [" + formatColor(this.colors[1]) + "] [" + formatColor(this.colors[2]) + "]\n",
                        "\t\t[##########]"
                    }
            );
        }

        Main.WINDOW_TITLE += " (DrawCalls: " + Main.NUMBER_OF_DRAWCALLS + ", Vertices: " + Main.NUMBER_OF_VERTICES + ")";
        Main.WINDOW_TITLE += " (x:" + (int) Math.floor(camera.getPosition().x()) + ",y:" + (int) Math.floor(camera.getPosition().y()) + ",z:" + (int) Math.ceil(camera.getPosition().z()) + ")";
        if (!this.textEnabled) {
            Main.WINDOW_TITLE += " (T - Show Wall of Text)";
        }
    }

    private String format(double d) {
        return String.format("%.2f", d);
    }

    private String formatColor(float f) {
        return String.format("%.1f", f);
    }

    public void mouseCursorMoved(double x, double y) {
        camera.mouseCursorMoved(x, y);
    }

    public void windowSizeChanged(int width, int height) {
        camera.setDimensions(width, height);
    }

    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_F && action == GLFW_PRESS) {
            Vector3dc camPos = camera.getPosition();
            Matrix4f model = new Matrix4f()
                    .translate((float) camPos.x(), (float) camPos.y() - 0.6f, (float) camPos.z())
                    .rotateXYZ(
                            (float) (Math.random() * (Math.PI * 2.0)),
                            (float) (Math.random() * (Math.PI * 2.0)),
                            (float) (Math.random() * (Math.PI * 2.0))
                    )
                    .scale((float) (Math.random() * 2.5) + 0.5f);
            cubes.add(new Cube(model, false));
        }
        if (key == GLFW_KEY_R && action == GLFW_PRESS) {
            if (cubes.size() > 1) {
                Cube c = cubes.remove(cubes.size() - 1);
                if (c != null) {
                    c.free();
                }
            }
        }
        if (key == GLFW_KEY_L && action == GLFW_PRESS) {
            for (Light l : lights) {
                l.renderShadowmap(cubes);
            }
            for (Cube c : cubes) {
                if (c != null) {
                    c.updateLightmap(lights);
                }
            }
            for (Light l : lights) {
                l.freeShadowmap();
            }
        }
        if (key == GLFW_KEY_X && action == GLFW_PRESS) {
            boolean enabled = this.sun.isEnabled();
            this.sun.setEnabled(!enabled);

            if (enabled) {
                DEFAULT_CLEAR_COLOR.set(0f, 0f, 0f);
            } else {
                DEFAULT_CLEAR_COLOR.set(0.2f, 0.4f, 0.6f);
            }
            glClearColor(DEFAULT_CLEAR_COLOR.x(), DEFAULT_CLEAR_COLOR.y(), DEFAULT_CLEAR_COLOR.z(), 1.0f);
        }
        if (key == GLFW_KEY_B && action == GLFW_PRESS) {
            if (this.lights.size() > 1) {
                Light light = this.lights.remove(this.lights.size()-1);
                light.freeShadowmap();
            }
        }
        if (key == GLFW_KEY_O && action == GLFW_PRESS) {
            this.currentComponent++;
            if (this.currentComponent == 3) {
                this.currentComponent = 0;
            }
        }
        if (key == GLFW_KEY_P && action == GLFW_PRESS) {
            this.colors[this.currentComponent] += 0.1f;
            if (this.colors[this.currentComponent] > 1.0001f) {
                this.colors[this.currentComponent] = 0.0f;
            }
        }
        if (key == GLFW_KEY_T && action == GLFW_PRESS) {
            this.textEnabled = !this.textEnabled;
        }
    }

    public void mouseCallback(long window, int button, int action, int mods) {
        
    }
}
