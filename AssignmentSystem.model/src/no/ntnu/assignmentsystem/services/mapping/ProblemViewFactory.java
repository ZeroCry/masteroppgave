package no.ntnu.assignmentsystem.services.mapping;

import no.ntnu.assignmentsystem.model.CodeProblem;
import no.ntnu.assignmentsystem.model.Problem;
import no.ntnu.assignmentsystem.model.QuizProblem;
import no.ntnu.assignmentsystem.model.Student;
import no.ntnu.assignmentsystem.services.CodeProblemView;
import no.ntnu.assignmentsystem.services.ExtendedProblemView;
import no.ntnu.assignmentsystem.services.ProblemView;import no.ntnu.assignmentsystem.services.QuizProblemView;
import no.ntnu.assignmentsystem.services.SourceCodeFileView;


public class ProblemViewFactory extends BaseViewFactory {
	public static ProblemView createProblemView(Student student, Problem problem) {
		ProblemView problemView = getFactory().createProblemView();
		mapProblemViewProperties(problemView, student, problem);
		return problemView;
	}
	
	public static ExtendedProblemView createExtendedProblemView(Student student, Problem problem) {
		ExtendedProblemView problemView = (problem instanceof CodeProblem) ? createCodeProblemView(student, (CodeProblem)problem) : createQuizProblemView(student, (QuizProblem)problem);
		mapProblemViewProperties(problemView, student, problem);
		return problemView;
	}
	
	
	// --- Private methods ---
	
	private static void mapProblemViewProperties(ProblemView problemView, Student student, Problem problem) {
		Mapper.copyAttributes(problem, problemView);
		
		// TODO: Map progress
	}
	
	private static CodeProblemView createCodeProblemView(Student student, CodeProblem codeProblem) {
		CodeProblemView codeProblemView = getFactory().createCodeProblemView();
		
		codeProblem.getSourceCodeFiles().forEach(sourceCodeFile -> {
			SourceCodeFileView sourceCodeFileView = getFactory().createSourceCodeFileView();
			Mapper.copyAttributes(sourceCodeFile, sourceCodeFileView);
			
			codeProblemView.getSourceCodeFiles().add(sourceCodeFileView);
		});
		
		return codeProblemView;
	}
	
	private static QuizProblemView createQuizProblemView(Student student, QuizProblem quizProblem) {
		QuizProblemView quizProblemView = getFactory().createQuizProblemView();
		
		return quizProblemView;
	}
}
