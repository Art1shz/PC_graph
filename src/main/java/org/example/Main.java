package org.example;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private long window;

    private Random random = new Random();

    private static final int NUM_STARS = 250;
    private float[][] stars = new float[NUM_STARS][3];

    public static void main(String[] args) {
        new Main().run();
    }

    private void initStars() {
        for (int i = 0; i < NUM_STARS; i++) {
            stars[i][0] = random.nextFloat() * 2 - 1;
            stars[i][1] = random.nextFloat() * 2 - 1;
            stars[i][2] = random.nextFloat() * 5 + 1;
        }
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Невозможно инициализировать GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(500, 500, "Starfield", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Ошибка при создание GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        initStars();
    }

    private void update() {
        float speed = 0.01f;

        for (int i = 0; i < NUM_STARS; i++) {
            stars[i][2] += speed;
            if (stars[i][2] >= 5) {
                stars[i][0] = random.nextFloat() * 2 - 1;
                stars[i][1] = random.nextFloat() * 2 - 1;
                stars[i][2] = 0;
            }
        }

        for (int i = 0; i < NUM_STARS; i++) {
            float depth = stars[i][2];
            float size = (1f + depth) * (1f + depth) * 0.1f;
            glPointSize(size);
        }
    }

    private void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glBegin(GL_POINTS);
        glColor3f(1.0f, 1.0f, 1.0f);
        for (int i = 0; i < NUM_STARS; i++) {
            glVertex3f(stars[i][0], stars[i][1], stars[i][2]);
        }
        glEnd();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            update();
            draw();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public void run() {
        System.out.println("Starfield");

        init();
        loop();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
