package plugin.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import plugin.views.TestSiteView;

public class SaveSelectedSiteHandler extends AbstractHandler{

	private static int numOfCompilationUnits;
	private static ExecutionEvent event;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		SaveSelectedSiteHandler.event = event;
		SaveSelectedSiteHandler.numOfCompilationUnits = 0;
		ISelection is = HandlerUtil.getCurrentSelectionChecked(event);
		IStructuredSelection iss;
		
		if(is instanceof IStructuredSelection)
			 iss = (IStructuredSelection)is;
		else 
			return null;
		
		Iterator it = iss.iterator();
		
		//�^�X�N���̃J�E���g
		while(it.hasNext()){
			Object selectionObject = it.next();
			if (!(selectionObject instanceof IJavaElement))
				continue;
			IJavaElement javaElement = (IJavaElement) selectionObject;
			switch(javaElement.getElementType()){
				case IJavaElement.COMPILATION_UNIT:
					numOfCompilationUnits++;
					break;
				case IJavaElement.PACKAGE_FRAGMENT:
					IPackageFragment pf = (IPackageFragment) javaElement;
					numOfCompilationUnits += getNumOfCompilationUnits(pf);
					break;
				case IJavaElement.PACKAGE_FRAGMENT_ROOT:
					IPackageFragmentRoot pfr = (IPackageFragmentRoot) javaElement;
					numOfCompilationUnits += getNumOfCompilationUnits(pfr);
					break;
				case IJavaElement.JAVA_PROJECT:
					IJavaProject pjt = (IJavaProject) javaElement;
					numOfCompilationUnits += getNumOfCompilationUnits(pjt);
					break;
				default:
					break;
			}
		}
		
		boolean yn = MessageDialog.openQuestion( HandlerUtil.getActiveShell(event), "�^�X�N�̈ꊇ�ǉ�", "���v" + numOfCompilationUnits+ "�̃N���X�����݂��܂��B�ǉ����܂����H");
		if(yn == false){
			return null;
		}
		
		
		SaveSelectedSiteJob job = new SaveSelectedSiteJob(event, iss, numOfCompilationUnits);
		job.addJobChangeListener(new JobChangeAdapter() {
	        public void done(IJobChangeEvent event) {
	        if (event.getResult().isOK())
	        	showResults();
	           else
	              System.out.println("Job did not complete successfully");
	        }
	     });
		job.setUser(true);//���[�U�[�Ƀ|�b�v�A�b�v��\������B
		job.setPriority(Job.SHORT);//higher priority than the default (Job.LONG). 
		job.schedule();//�W���u��Eclipse�ɓo�^���Ă����܂��B���Ƃ�Eclipse���K�؂�run�����s���A�������Ă����
//		try {
//			job.join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		refreshView();
//		MessageDialog.openInformation( HandlerUtil.getActiveShell(event), "�^�X�N�̒ǉ�����", "���v" +job.getNumOfSites()+ "�̃^�X�N��ǉ����܂����B");
		
		return null;
	}

	
	private int getNumOfCompilationUnits(IJavaProject p){
		int sum = 0;
		try {
			IPackageFragmentRoot []root_arr = p.getPackageFragmentRoots();
			for(IPackageFragmentRoot root: root_arr){
				sum += getNumOfCompilationUnits(root);
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sum;
	}
	
	private int getNumOfCompilationUnits(IPackageFragmentRoot pfr){
		int sum = 0;
		try {
			if (pfr.getKind() == IPackageFragmentRoot.K_SOURCE){
				for(IJavaElement je: pfr.getChildren()){
					sum += getNumOfCompilationUnits((IPackageFragment)je);
				}
				
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sum;
	}
	
	private int getNumOfCompilationUnits(IPackageFragment pf){
		int sum = 0;
		try {
			ICompilationUnit []icu_arr = pf.getCompilationUnits();
			sum = icu_arr.length;
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sum;
	}
	
	private static void refreshView(){
		 //�r���[�̃��t���b�V��
		TestSiteView view;
		try {
			view = (TestSiteView) HandlerUtil.getActiveWorkbenchWindowChecked(event).getActivePage().findView("jp.ac.hokudai.eng.complex.kussharo.sayuu.kp.testSiteView");
			view.refresh();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	 protected static void showResults() {
	        Display.getDefault().asyncExec(new Runnable() {
	           public void run() {
	              getReservationCompletedAction().run();
	           }
	        });
	     }

	protected static Action getReservationCompletedAction() {
		return new Action("�w�K�^�X�N�̈ꊇ�ǉ� �I��") {
			public void run() {
				try {
					MessageDialog.openInformation(getShell(), 
					  "�w�K�^�X�N�̈ꊇ�ǉ�", 
					  "�w�K�^�X�N�̒ǉ����������܂����B");
					refreshView();
				} catch (ExecutionException e) {
					// TODO �����������ꂽ catch �u���b�N
					e.printStackTrace();
				}
			}
		};
	}
	
	protected static Shell getShell() throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		return window.getShell();
	}

}