package org.rsbot.service;

import org.rsbot.util.GlobalConfiguration;

import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.SecureClassLoader;

/**
 * @author Jacmob
 */
class ScriptClassLoader extends SecureClassLoader {

	private final URL base;

	public ScriptClassLoader(final URL url) {
		base = url;
	}

	@Override
	protected PermissionCollection getPermissions(final CodeSource codesource) {
		final Permissions perms = new Permissions();
		perms.add(new FilePermission("<<ALL FILES>>", ""));
		perms.add(new FilePermission(GlobalConfiguration.Paths.getScriptCacheDirectory() + File.separator + "/-", "read,write,delete"));
		perms.add(new SocketPermission("*.powerbot.org:80", "connect"));
		perms.add(new SocketPermission("*.imageshack.us:80", "connect"));
		perms.add(new SocketPermission("*.tinypic.com:80", "connect"));
		perms.add(new SocketPermission("*.photobucket.com:80", "connect"));
		perms.add(new SocketPermission("i.imgur.com:80", "connect"));
		return perms;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		Class clazz = findLoadedClass(name);

		if (clazz == null) {
			try {
				final InputStream in = getResourceAsStream(name.replace('.', '/') + ".class");
				final byte[] buffer = new byte[4096];
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				int n;
				while ((n = in.read(buffer, 0, 4096)) != -1) {
					out.write(buffer, 0, n);
				}
				final byte[] bytes = out.toByteArray();
				clazz = defineClass(name, bytes, 0, bytes.length);
				if (resolve) {
					resolveClass(clazz);
				}
			} catch (final Exception e) {
				clazz = super.loadClass(name, resolve);
			}
		}

		return clazz;
	}

	@Override
	public URL getResource(final String name) {
		try {
			return new URL(base, name);
		} catch (final MalformedURLException e) {
			return null;
		}
	}

	@Override
	public InputStream getResourceAsStream(final String name) {
		try {
			return new URL(base, name).openStream();
		} catch (final IOException e) {
			return null;
		}
	}

}
