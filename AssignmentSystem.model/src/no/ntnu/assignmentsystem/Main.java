package no.ntnu.assignmentsystem;

import java.io.File;

import no.ntnu.assignmentsystem.model.ModelLoader;
import no.ntnu.assignmentsystem.model.XmiModelLoader;
import no.ntnu.assignmentsystem.services.Services;
import no.ntnu.assignmentsystem.services.ServicesImpl;

public class Main {
	public static void main(String[] args) {
		ModelLoader modelLoader = new XmiModelLoader(new File("model/UoD.xmi"));
		Services services = new ServicesImpl(modelLoader);
//		System.out.println(services.getAssignments(""));
//		System.out.println(services.getProblem("1", "a"));
		System.out.println(services.runProblem("1", "b"));
	}
}
