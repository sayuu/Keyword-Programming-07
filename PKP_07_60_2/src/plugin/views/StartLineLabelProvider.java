package plugin.views;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import plugin.testSite.TestSite;

public class StartLineLabelProvider extends CellLabelProvider{

	@Override
	public void update(ViewerCell cell) {
		// TODO Auto-generated method stub
		TestSite site = (TestSite)cell.getElement();
		//0から始まるので1を追加して表示する。
		cell.setText(String.valueOf(site.getLineNumberStart() + 1));
	}

}
