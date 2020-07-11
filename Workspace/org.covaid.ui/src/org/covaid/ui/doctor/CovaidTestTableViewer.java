package org.covaid.ui.doctor;

import java.util.ArrayList;
import java.util.Collection;

import org.condast.commons.strings.StringStyler;
import org.condast.commons.ui.widgets.AbstractTableViewerWithDelete;
import org.condast.commons.ui.widgets.IStoreWithDelete;
import org.covaid.core.data.DoctorData;
import org.covaid.core.doctor.DoctorDataEvent;
import org.covaid.core.doctor.IDoctorDataListener;
import org.covaid.ui.images.CovaidImages;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class CovaidTestTableViewer extends AbstractTableViewerWithDelete<DoctorData>{
	private static final long serialVersionUID = 1L;

	private enum Columns{
		ID,
		ILL;

		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString() );//NaLanguage.getInstance().getString( this );
		}

		public static int getWeight( Columns column ){
			switch( column ){
			case ID:
			case ILL:
				return 30;
			default:
				return 10;
			}
		}
	}

	private Collection<IDoctorDataListener> dlisteners;

	public CovaidTestTableViewer(Composite parent,int style ) {
		super(parent,style, true );
		this.dlisteners = new ArrayList<>();
	}

	public void addSelectionChangeListener( ISelectionChangedListener listener ) {
		TableViewer viewer = super.getViewer();
		viewer.addSelectionChangedListener(listener);
	}

	public void removeSelectionChangeListener( ISelectionChangedListener listener ) {
		TableViewer viewer = super.getViewer();
		viewer.remove(listener);
	}

	public void addDoctorListener(IDoctorDataListener listener) {
		this.dlisteners.add(listener);
	}

	public void removeDoctorListener(IDoctorDataListener listener) {
		this.dlisteners.remove(listener);
	}

	@Override
	protected void createContentComposite( Composite parent,int style ){
		super.createContentComposite(parent, style);
		TableViewer viewer = super.getViewer();
		for( Columns column: Columns.values() ){
			createColumn( column );
		}
		String deleteStr = Buttons.DELETE.toString();
		super.createDeleteColumn( Columns.values().length, deleteStr, 10 );	
		viewer.setLabelProvider( new DoctorDataLabelProvider() );
	}

	public void setInput( Collection<DoctorData> data ){
		super.setInput( data );
	}
	
	public int getSelectionIndex( DoctorData vessel ) {
		Collection<IStoreWithDelete<DoctorData>> vessels = this.getStoreInput();
		int index = 0;
		for( IStoreWithDelete<DoctorData> rvessel: vessels ) {
			if( rvessel.getStore().equals( vessel))
				return index;
			index++;
		}
		return index;
	}

	@Override
	protected void onRowDoubleClick(DoctorData selection) {
	}

	@Override
	protected void onButtonCreated(Buttons type, Button button) {
		GridData gd_button = new GridData(32, 32);
		gd_button.horizontalAlignment = SWT.RIGHT;
		button.setLayoutData(gd_button);
		button.setText("");
	}

	@Override
	protected boolean onAddButtonSelected(SelectionEvent e) {
		boolean result = false;
		try{
		}
		catch( Exception ex ){
			ex.printStackTrace();
		}
		return result;
	}
	
	@Override
	protected boolean onDeleteButton( Collection<DoctorData> deleted ) {
		for( IDoctorDataListener listener: this.dlisteners )
			for( DoctorData data: deleted )
				listener.notifyDoctorDoctorChanged( new DoctorDataEvent(this, IDoctorDataListener.DocterDataEvents.REMOVE, data));
		return true;
	}

	private TableViewerColumn createColumn( final Columns column ) {
		TableViewerColumn result = super.createColumn( column.toString(), column.ordinal(), Columns.getWeight(column) );
		return result;
	}
	
	@Override
	protected void onRefresh() {
		//super.setInput( Arrays.asList( manager.getVessels() ));
	}
	
	private class DoctorDataLabelProvider extends DeleteLabelProvider implements ITableLabelProvider{
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		@Override
		public String getColumnText( Object element, int columnIndex ) {
			String retval = super.getColumnText(element, columnIndex);
			if( retval != null )
				return retval;
			Columns column = Columns.values()[ columnIndex ];
			IStoreWithDelete<DoctorData> swd = (IStoreWithDelete<DoctorData>) element;
			DoctorData data = swd.getStore();
			switch( column){
			case ID:
				retval = String.valueOf( data.getId());
				break;
			case ILL:
				retval = String.valueOf( data.hasCovaid());
				break;
			default:
				break;				
			}
			swd.addText(retval);
			return retval;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Image getColumnImage(Object arg0, int columnIndex) {
			Image image = super.getColumnImage(arg0, columnIndex);
			if( columnIndex == getDeleteColumnindex() ){
				return image;
			}
			Columns column = Columns.values()[ columnIndex ];
			IStoreWithDelete<DoctorData> swd = (IStoreWithDelete<DoctorData>) arg0;
			DoctorData data = swd.getStore();
			CovaidImages images = CovaidImages.getInstance();
			switch( column){
			case ILL:
				image = data.hasCovaid()? images.getImage( CovaidImages.Images.CHECK): image;
				break;
			default:
				image = super.getColumnImage(arg0, columnIndex);
				break;				
			}
			return image;
		}
	}
}