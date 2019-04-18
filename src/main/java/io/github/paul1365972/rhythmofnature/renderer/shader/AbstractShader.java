package io.github.paul1365972.rhythmofnature.renderer.shader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.stream.Collectors;

public abstract class AbstractShader {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	protected int programID = -1;
	protected int vertexShader = -1;
	protected int fragmentShader = -1;
	
	private FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	
	public AbstractShader(String name) {
		String code = read(name);
		if (code != null) {
			programID = GL20.glCreateProgram();
			bindAttributes();
			String[] shaders = code.split("type:");
			for (int i = 1; i < shaders.length; i++) {
				if (shaders[i].startsWith("vertex")) {
					if (vertexShader != -1)
						throw new IllegalArgumentException("Double definition of VertexShader");
					vertexShader = loadShader(shaders[i].substring("vertex".length()), GL20.GL_VERTEX_SHADER);
				} else if (shaders[i].startsWith("fragment")) {
					if (fragmentShader != -1)
						throw new IllegalArgumentException("Double definition of FragmentShader");
					fragmentShader = loadShader(shaders[i].substring("fragment".length()), GL20.GL_FRAGMENT_SHADER);
				} else {
					LOGGER.error("Unsupported Shader: " + shaders[i]);
				}
			}
			
			
			GL20.glLinkProgram(programID);
			LOGGER.debug("Creating Program");
			if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) != GL11.GL_TRUE)
				LOGGER.error("Couldnt compile Program: " + GL20.glGetProgramInfoLog(programID));
			
			GL20.glValidateProgram(programID);
			if (GL20.glGetProgrami(programID, GL20.GL_VALIDATE_STATUS) != GL11.GL_TRUE)
				LOGGER.error("Couldnt validate Program: " + GL20.glGetProgramInfoLog(programID));
			
			if (vertexShader != -1)
				GL20.glDeleteShader(vertexShader);
			if (fragmentShader != -1)
				GL20.glDeleteShader(fragmentShader);
			
			getAllUniformLocations();
			start();
			connectTextureUnits();
			stop();
		}
	}
	
	private static String read(String name) {
		String path = "/shaders/" + name;
		try (InputStream is = AbstractShader.class.getResourceAsStream(path)) {
			if (is != null) {
				return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
			} else {
				LOGGER.error("Couldnt read Shaderfile: " + path);
			}
		} catch (IOException e) {
			LOGGER.error("Couldnt find Shaderfile: " + path);
		}
		return null;
	}
	
	private int loadShader(String code, int shaderType) {
		int id = GL20.glCreateShader(shaderType);
		GL20.glShaderSource(id, code);
		GL20.glCompileShader(id);
		if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE)
			LOGGER.error("Couldnt compile Shader: " + GL20.glGetShaderInfoLog(id));
		GL20.glAttachShader(programID, id);
		
		return id;
	}
	
	protected int getUniformLocation(String uniformName) {
		return GL20.glGetUniformLocation(programID, uniformName);
	}
	
	protected abstract void bindAttributes();
	
	protected abstract void getAllUniformLocations();
	
	protected abstract void connectTextureUnits();
	
	public void cleanUp() {
		stop();
		if (vertexShader != -1)
			GL20.glDetachShader(programID, vertexShader);
		if (fragmentShader != -1)
			GL20.glDetachShader(programID, fragmentShader);
		if (programID != -1)
			GL20.glDeleteProgram(programID);
	}
	
	protected void bindAttribute(int attribute, String name) {
		GL20.glBindAttribLocation(programID, attribute, name);
	}
	
	protected void loadInt(int location, int v) {
		GL20.glUniform1i(location, v);
	}
	
	protected void loadFloat(int location, float v) {
		GL20.glUniform1f(location, v);
	}
	
	protected void loadVector(int location, float x, float y) {
		GL20.glUniform2f(location, x, y);
	}
	
	protected void loadVector(int location, float x, float y, float z) {
		GL20.glUniform3f(location, x, y, z);
	}
	
	protected void loadVector(int location, float x, float y, float z, float w) {
		GL20.glUniform4f(location, x, y, z, w);
	}
	
	protected void loadMatrix(int location, Matrix4f matrix) {
		buffer.clear();
		matrix.get(buffer);
		GL20.glUniformMatrix4fv(location, false, buffer);
	}
	
	protected void loadMatrices(int location, Matrix4f[] matrices) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16 * matrices.length);
		for (int i = 0; i < matrices.length; i++) {
			matrices[i].get(i * 16, buffer);
		}
		GL20.glUniformMatrix4fv(location, false, buffer);
	}
	
	protected void loadAsyncMatrix(int location, Matrix4f matrix) {
		FloatBuffer asyncBuffer = BufferUtils.createFloatBuffer(16);
		matrix.get(asyncBuffer);
		GL20.glUniformMatrix4fv(location, false, asyncBuffer);
	}
	
	protected void loadVectori(int location, int x, int y, int z) {
		GL20.glUniform3i(location, x, y, z);
	}
	
	protected void bindTextureUnit(String name, int unit) {
		loadInt(getUniformLocation(name), unit);
	}
	
	public void start() {
		GL20.glUseProgram(programID);
	}
	
	public void stop() {
		GL20.glUseProgram(0);
	}
}
