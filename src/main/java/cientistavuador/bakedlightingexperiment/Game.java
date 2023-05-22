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
import cientistavuador.bakedlightingexperiment.cube.light.icon.IconRender;
import cientistavuador.bakedlightingexperiment.cube.light.point.PointLight;
import cientistavuador.bakedlightingexperiment.cube.light.spot.SpotLight;
import cientistavuador.bakedlightingexperiment.ubo.CameraUBO;
import cientistavuador.bakedlightingexperiment.ubo.UBOBindingPoints;
import cientistavuador.bakedlightingexperiment.debug.AabRender;
import cientistavuador.bakedlightingexperiment.text.GLFontRenderer;
import cientistavuador.bakedlightingexperiment.text.GLFontSpecification;
import cientistavuador.bakedlightingexperiment.text.GLFontSpecifications;
import java.awt.Color;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JColorChooser;
import org.joml.Matrix4f;
import org.joml.Vector3dc;
import org.joml.Vector3f;
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
    private final float[] colors = {1f, 1f, 1f};
    private int currentComponent = 0;
    private boolean textEnabled = true;
    private boolean colorChooserOpen = false;
    private boolean whereIsIt = false;
    private boolean hideIcons = false;
    private float brightness = 4.0f;
    private float spotAngle = 60.0f;

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

        IconRender.begin(cameraProjectionView);

        if (!this.hideIcons) {
            for (Light light : this.lights) {
                IconRender.render(
                        light,
                        this.camera.getView()
                );
            }

            IconRender.finish();
        }

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
                                .append("\tR - Remove Last Cube").append(" [").append(this.cubes.size() - 1).append(" Cubes]\n")
                                .append("\tL - Update Lightmap").append(" [").append(this.lights.size() - 1).append(" Lights]\n")
                                .append("\tX - Enable/Disable Sun. [").append(sun.isEnabled() ? "Enabled" : "Disabled").append("]\n")
                                .append("\tShift + Left Click - Place Spotlight.\n")
                                .append("\tShift + Right Click - Place Point Light.\n")
                                .append("\tB - Remove Last Light.\n")
                                .append("\tT - Hide This Wall of Text.\n")
                                .append("\tI - ").append(this.hideIcons ? "Show" : "Hide").append(" Light Icons.")
                                .toString(),
                        "\nLight Color\n",
                        new StringBuilder()
                                .append("\tC - Open Color Chooser").append(this.whereIsIt ? " (BEHIND THE WINDOW!)" : "").append("\n")
                                .append("\tUp/Down Arrow (+Shift x10) - Brightness: ").append(formatColor(this.brightness)).append("\n")
                                .append("\tLeft/Right Arrow (+Shift x10) - Spotlight Angle: ").append(formatColor(this.spotAngle)).append("\n")
                                .append("\tO - Next Component\n")
                                .append("\tP - Increase/Decrease Component\n").append("\t\t").append(componentSelected).append("\n\t\t[").append(formatColor(this.colors[0])).append("] [").append(formatColor(this.colors[1])).append("] [").append(formatColor(this.colors[2])).append("]\n")
                                .toString(),
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
            for (Cube c : this.cubes) {
                c.clearLightmap();
            }
            for (Light l : this.lights) {
                if (!l.isEnabled()) {
                    continue;
                }
                l.renderShadowMap(this.cubes);
                for (Cube c : this.cubes) {
                    c.updateLightmap(l);
                }
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
                this.lights.remove(this.lights.size() - 1);
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
        if (key == GLFW_KEY_C && action == GLFW_PRESS) {
            if (!this.colorChooserOpen) {
                this.colorChooserOpen = true;
                Thread e = new Thread(() -> {
                    Color chosen = JColorChooser.showDialog(null, "RGB", Color.WHITE, false);
                    if (chosen != null) {
                        Main.MAIN_TASKS.add(() -> {
                            float r = chosen.getRed() / 255f;
                            float g = chosen.getGreen() / 255f;
                            float b = chosen.getBlue() / 255f;
                            this.colors[0] = r;
                            this.colors[1] = g;
                            this.colors[2] = b;
                            this.colorChooserOpen = false;
                            this.whereIsIt = false;
                        });
                    } else {
                        Main.MAIN_TASKS.add(() -> {
                            this.colorChooserOpen = false;
                            this.whereIsIt = false;
                        });
                    }
                });
                e.setDaemon(true);
                e.start();
            } else {
                Toolkit.getDefaultToolkit().beep();
                this.whereIsIt = true;
            }
        }
        if (key == GLFW_KEY_I && action == GLFW_PRESS) {
            this.hideIcons = !this.hideIcons;
        }
        if ((key == GLFW_KEY_UP || key == GLFW_KEY_DOWN) && (action == GLFW_PRESS || action == GLFW_REPEAT)) {
            float multiplier = 1f;
            if ((mods & GLFW_MOD_SHIFT) != 0) {
                multiplier = 10f;
            }
            if (key == GLFW_KEY_UP) {
                this.brightness += 0.1f * multiplier;
            } else {
                this.brightness -= 0.1f * multiplier;
            }
            if (this.brightness > 50f) {
                this.brightness = 50f;
            }
            if (this.brightness < 0.1f) {
                this.brightness = 0.1f;
            }
        }
        if ((key == GLFW_KEY_LEFT || key == GLFW_KEY_RIGHT) && (action == GLFW_PRESS || action == GLFW_REPEAT)) {
            float multiplier = 1f;
            if ((mods & GLFW_MOD_SHIFT) != 0) {
                multiplier = 10f;
            }
            if (key == GLFW_KEY_RIGHT) {
                this.spotAngle += 0.1f * multiplier;
            } else {
                this.spotAngle -= 0.1f * multiplier;
            }
            if (this.spotAngle > 85f) {
                this.spotAngle = 85f;
            }
            if (this.spotAngle < 10f) {
                this.spotAngle = 10f;
            }
        }
    }

    public void mouseCallback(long window, int button, int action, int mods) {
        if (button == GLFW_MOUSE_BUTTON_RIGHT && (mods & GLFW_MOD_SHIFT) != 0 && action == GLFW_PRESS) {
            PointLight light = new PointLight(
                    new Vector3f().set(this.camera.getPosition()),
                    new Vector3f(this.colors),
                    this.brightness
            );
            this.lights.add(light);
        }
        if (button == GLFW_MOUSE_BUTTON_LEFT && (mods & GLFW_MOD_SHIFT) != 0 && action == GLFW_PRESS) {
            SpotLight light = new SpotLight(
                    new Vector3f().set(this.camera.getPosition()),
                    new Vector3f(this.camera.getFront()),
                    new Vector3f(this.colors),
                    this.spotAngle,
                    this.brightness
            );
            this.lights.add(light);
        }
    }
}
