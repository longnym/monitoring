package com.sk.collect.monitor.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.springframework.stereotype.Service;

@Service
public class LauncherService {
	// Command를 실행하여 결과를 받음
	public String executeCommand(String cmd) {
		StringBuffer output = new StringBuffer();

		try {
			Process p = Runtime.getRuntime().exec(cmd);
			if (!p.waitFor(5, TimeUnit.SECONDS)) {
				p.destroy();
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return output.toString();
	}

	// Jar Class를 실행하여 결과를 받음
	public String runJar(String filePath, String option) {
		String JAR_URL = "jar:file:" + filePath + "!/";
		String JAR_FILE_PATH = "file:" + filePath;

		ByteArrayOutputStream bstream = new ByteArrayOutputStream();

		try {
			URL FileSysUrl = new URL(JAR_URL);
			JarURLConnection jarURLConnection = (JarURLConnection) FileSysUrl.openConnection();
			JarFile jarFile = jarURLConnection.getJarFile();

			System.out.println("Jar Name: " + jarFile.getName());
			System.out.println("\nJar Entry: " + jarURLConnection.getJarEntry());

			Manifest manifest = jarFile.getManifest();

			URL[] classLoaderUrls = new URL[] { new URL(JAR_FILE_PATH) };
			URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);

			String mainClassName = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);

			Class<?> beanClass = urlClassLoader.loadClass(mainClassName);
			Method method = beanClass.getMethod("main", String[].class);

			PrintStream newPs = new PrintStream(bstream);
			PrintStream oldPs = System.out;
			System.setOut(newPs);

			String[] params = option.split(" ");
			method.invoke(null, (Object) params);

			System.out.flush();
			System.setOut(oldPs);

			urlClassLoader.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return bstream.toString();
	}
}