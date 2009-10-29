/*
 * swingx - Swing eXtensions
 * Copyright (C) 2004 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package swingx.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The transferable for arbitrary objects.
 */
public class ObjectTransferable implements Transferable {

	/**
	 * The dataFlavor used for transfers between different JVMs.
	 */
	public static DataFlavor serializedFlavor = new DataFlavor(
			Serializable.class, "Object");

	/**
	 * The dataFlavor used for transfers in one JVM.
	 */
	public static DataFlavor localFlavor;

	private List<DataFlavor> flavors;

	private Object object;

	public ObjectTransferable(Object object) {
		this.object = object;

		flavors = createFlavors(object);
	}

	protected List<DataFlavor> createFlavors(Object object) {

		List<DataFlavor> flavors = new ArrayList<DataFlavor>();

		flavors.add(localFlavor);

		boolean serializable = true;
		if (object.getClass().isArray()) {
			Object[] array = (Object[]) object;
			for (int n = 0; n < array.length; n++) {
				serializable = serializable
						&& (array[n] instanceof Serializable);
			}
		} else {
			serializable = object instanceof Serializable;
		}
		if (serializable) {
			flavors.add(serializedFlavor);
		}

		return flavors;
	}

	public void clear() {
		this.object = null;
	}
	
	private boolean hasObject() {
		return object != null;
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException {

		if (hasObject()) {
			if (localFlavor.equals(flavor)) {
				return object;
			}
			if (serializedFlavor.equals(flavor)) {
				return object;
			}
		}

		throw new UnsupportedFlavorException(flavor);
	}

	public DataFlavor[] getTransferDataFlavors() {
		if (hasObject()) {
			return flavors.toArray(new DataFlavor[flavors.size()]);
		}

		return new DataFlavor[0];
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (hasObject()) {
			return flavors.contains(flavor);
		}

		return false;
	}

	public static Object getObject(Transferable transferable)
			throws UnsupportedFlavorException, IOException {
		if (transferable.isDataFlavorSupported(localFlavor)) {
			return transferable.getTransferData(localFlavor);
		} else if (transferable.isDataFlavorSupported(serializedFlavor)) {
			return transferable.getTransferData(serializedFlavor);
		}
		throw new IOException();
	}

	static {
		try {
			localFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
					+ ";class=java.lang.Object");
		} catch (ClassNotFoundException e) {
			throw new Error(e);
		}
	}
}