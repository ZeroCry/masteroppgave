package no.ntnu.assignmentsystem.model.impl;

import no.ntnu.assignmentsystem.model.GlobalIdManager;
import no.ntnu.assignmentsystem.model.ModelLoader;

public class GlobalIdManagerImpl implements GlobalIdManager {
	private final ModelLoader modelLoader;
	
	public GlobalIdManagerImpl(ModelLoader modelLoader) {
		this.modelLoader = modelLoader;
	}

	@Override
	public String generateId() {
		// TODO: Make this code thread-safe
		int lastGlobalId = modelLoader.getUoD().getLastGlobalId() + 1;
		modelLoader.getUoD().setLastGlobalId(lastGlobalId);
		
		return String.valueOf(lastGlobalId);
	}

}
