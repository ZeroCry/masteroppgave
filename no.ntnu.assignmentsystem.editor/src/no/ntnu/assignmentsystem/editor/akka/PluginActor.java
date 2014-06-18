package no.ntnu.assignmentsystem.editor.akka;

import java.io.IOException;
import java.util.List;

import no.ntnu.assignmentsystem.editor.akka.mapping.PluginErrorCheckingResultMapper;
import no.ntnu.assignmentsystem.editor.akka.messages.PluginErrorCheckingResult;
import no.ntnu.assignmentsystem.editor.akka.messages.PluginReady;
import no.ntnu.assignmentsystem.editor.akka.messages.PluginRunMain;
import no.ntnu.assignmentsystem.editor.akka.messages.PluginRunMainResult;
import no.ntnu.assignmentsystem.editor.akka.messages.PluginRunTests;
import no.ntnu.assignmentsystem.editor.akka.messages.PluginRunTestsResult;
import no.ntnu.assignmentsystem.editor.akka.messages.PluginTestResult;
import no.ntnu.assignmentsystem.editor.akka.messages.PluginUpdateSourceCode;
import no.ntnu.assignmentsystem.editor.jdt.WorkspaceManager;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.junit.JUnitCore;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class PluginActor extends UntypedActor implements AkkaTestRunListener.Delegate, WorkspaceManager.Listener {
	private final ActorRef consumerActor;
	private final WorkspaceManager workspaceManager;
	
	public PluginActor(ActorRef consumerActor, WorkspaceManager workspaceManager) {
		System.out.println("Constructing PluginActor:" + this);
		
		this.consumerActor = consumerActor;
		this.workspaceManager = workspaceManager;
		
		workspaceManager.addListener(this); // TODO: Add removeListener
	}
	
	@Override
	public void preStart() {
		System.out.println(getSelf() + ": Pre-start");
		
		JUnitCore.addTestRunListener(new AkkaTestRunListener(this));
		
		consumerActor.tell(new PluginReady(), getSelf());
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		System.out.println(getSelf() + ": Received message:" + message);
		
		if (message instanceof PluginUpdateSourceCode) {
			handleUpdateSourceCode((PluginUpdateSourceCode)message);
		}
		else if (message instanceof PluginRunMain) {
			handleRunMain((PluginRunMain)message);
		}
		else if (message instanceof PluginRunTests) {
			handleRunTests((PluginRunTests)message);
		}
		
		else {
			unhandled(message);
		}
	}
	
	
	// --- Handlers ---
	
	private void handleRunMain(PluginRunMain runMain) throws CoreException {
		String result = workspaceManager.runMain(runMain.qualifiedClassName);
		getSender().tell(new PluginRunMainResult(result), getSelf());
	}
	
	private void handleRunTests(PluginRunTests runTests) throws CoreException {
		workspaceManager.runTests(runTests.qualifiedClassName);
		// NOTE: Result is sent from the testRunCompleted() delegate method
	}
	
	private void handleUpdateSourceCode(PluginUpdateSourceCode updateSourceCode) throws JavaModelException, CoreException, IOException {
		workspaceManager.updateSourceCode(updateSourceCode.packageName, updateSourceCode.fileName, updateSourceCode.sourceCode);
	}
	
	
	// --- AkkaTestRunListener.Delegate ---
	
	@Override
	public void testRunCompleted(AkkaTestRunListener listener, List<PluginTestResult> testResults) {
		consumerActor.tell(new PluginRunTestsResult(testResults), getSelf());
	}
	
	
	// --- WorkspaceManager.Listener ---
	
	@Override
	public void problemMarkerDidChange(String packageName, String fileName, IMarker[] markers) {
		System.out.println("Should notify about problems");
		System.out.println("Input:" + packageName + " " + fileName + " " + markers);
		PluginErrorCheckingResult errorCheckingResult = PluginErrorCheckingResultMapper.createErrorCheckingResult(packageName, fileName, markers);
		System.out.println("ErrorCheckingResult:" + errorCheckingResult);
		consumerActor.tell(errorCheckingResult, getSelf());
	}
}
