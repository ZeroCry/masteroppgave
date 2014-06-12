package no.ntnu.assignmentsystem.editor.jdt;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.junit.JUnitCore;
import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestCaseElement;
import org.eclipse.jdt.junit.model.ITestRunSession;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

public class WorkspaceManager {
	private static final String srcFolderName = "src";
	private static final String binFolderName = "bin"; 
	
	private final String projectName;
	
	private IProject _project;
	private IJavaProject _javaProject;
	private IPackageFragmentRoot _srcFolder;
	
	public WorkspaceManager(String projectName) {
		this.projectName = projectName;
	}
	
	public void updateSourceCode(String packageName, String fileName, String sourceCode) throws JavaModelException, CoreException {
		IPackageFragment fragment = getSrcFolder().createPackageFragment(packageName, true, null);
		ICompilationUnit compilationUnit = fragment.createCompilationUnit(fileName, sourceCode, false, null);
		compilationUnit.reconcile(ICompilationUnit.NO_AST, true, null, null);
	}
	
	public String runMain(String qualifiedClassName) throws CoreException {
		return launch(getJavaProject(), qualifiedClassName);
	}


	// --- Private methods ---
	
	private String launch(IJavaProject project, String qualifiedClassName) throws CoreException {
		IVMInstall virtualMachine = JavaRuntime.getVMInstall(project);
		if (virtualMachine == null) {
			virtualMachine = JavaRuntime.getDefaultVMInstall();
		}
		IVMRunner virtualMachineRunner = virtualMachine.getVMRunner(ILaunchManager.RUN_MODE);
		String[] classPath = JavaRuntime.computeDefaultRuntimeClassPath(project);
		VMRunnerConfiguration config = new VMRunnerConfiguration(qualifiedClassName, classPath);
		ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
		virtualMachineRunner.run(config, launch, null);
		
		while (!launch.isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
		}
	    
		String output = "";
		
		IProcess[] processes = launch.getProcesses();
		for (IProcess process : processes) {
			String errorStream = process.getStreamsProxy().getErrorStreamMonitor().getContents();
			if (errorStream != null && errorStream.length() > 0) {
				output += errorStream;
			}
			
			String outputStream = process.getStreamsProxy().getOutputStreamMonitor().getContents();
			if (outputStream != null && outputStream.length() > 0) {
				output += outputStream;
			}
		}
		
		return output;
	}

	private IJavaProject getJavaProject() throws CoreException {
		if (_javaProject == null) {
			_javaProject = JavaCore.create(getProject());
			
//			JavaRuntime.get
			
			IClasspathEntry[] buildPath = {
				JavaRuntime.getDefaultJREContainerEntry(),
				JavaCore.newContainerEntry(JUnitCore.JUNIT3_CONTAINER_PATH),
//				JavaCore.newLibraryEntry(path, null, null)
				JavaCore.newSourceEntry(getProject().getFullPath().append(srcFolderName))
			};
			
			_javaProject.setRawClasspath(buildPath, getProject().getFullPath().append(binFolderName), null);
		}
		
		return _javaProject;
	}
	
	private IPackageFragmentRoot getSrcFolder() throws CoreException {
		if (_srcFolder == null) {
			IFolder folder = getProject().getFolder(srcFolderName);
			folder.create(true, true, null);
			
			_srcFolder = getJavaProject().getPackageFragmentRoot(folder);
		}
		
		return _srcFolder;
	}
	
	private IProject getProject() throws CoreException {
		if (_project == null) {
			_project = getWorkspaceRoot().getProject(projectName);
			_project.create(null);
			_project.open(null);
	
			IProjectDescription description = _project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			_project.setDescription(description, null);
		}
		
		return _project;
	}
	
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
