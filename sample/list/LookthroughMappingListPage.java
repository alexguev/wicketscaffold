package ca.cppib.irm.web.pages.lookthrough;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ca.cppib.irm.servicesapi.lookthrough.ILookthroughMappingService;
import ca.cppib.irm.servicesapi.lookthrough.LookthroughMappingVO;
import ca.cppib.irm.servicesapi.security.PermCodeEnum;
import ca.cppib.irm.web.SecuredBasePage;
import ca.cppib.irm.web.authorizer.RequiresAtLeastOnePermAuthorizer;
import ca.cppib.irm.webconstants.IPageConstants;
import ca.intelliware.fs.wicket.link.SecuredBookmarkablePageLink;
import ca.intelliware.fs.wicket.utils.pageinterface.IFSPageInterface;
import ca.intelliware.fs.wicket.utils.table.DataTablePropertyLabelComponent;
import ca.intelliware.fs.wicket.utils.table.IDataTable;

public class LookthroughMappingListPage extends SecuredBasePage {
	@SpringBean
	private ILookthroughMappingService lookthroughMappingService;
	
	public static IFSPageInterface<LookthroughMappingListPage, ILookthroughMappingPageParameters> getPageInterface() {
		return createPageInterface(LookthroughMappingListPage.class, ILookthroughMappingPageParameters.class, 
				new RequiresAtLeastOnePermAuthorizer<ILookthroughMappingPageParameters>(PermCodeEnum.GTAA_VIEW, PermCodeEnum.GTAA_EDIT));
	}

	public LookthroughMappingListPage(PageParameters pageParameters) {
		super(pageParameters);
		
		ILookthroughMappingPageParameters parameters = getPageInterface().parse(pageParameters);
		
		IDataTable<LookthroughMappingVO> table = createLookthroughMappingTable(parameters.getChronId());
		add(table.createComponent());
		
		SecuredBookmarkablePageLink addLink = EditLookthroughMappingPage.getPageInterface().create().prepare().
				createBookmarkablePageLink("addNewLink", getStringResource("addNewLink", null));
		add(addLink);
		
		if (parameters.getChronId() != null) {
			addLink.setVisible(false);
		}

		SecuredBookmarkablePageLink returnToLookthroughLink = LookthroughMappingPage.getPageInterface().create().prepare().
				createBookmarkablePageLink("returnToLookthroughLink", getStringResource("returnToLookthroughLink", null));
		add(returnToLookthroughLink);
	}
	
	public IDataTable<LookthroughMappingVO> createLookthroughMappingTable(Long chronId) {
		LookthroughMappingDataProvider dataProvider = new LookthroughMappingDataProvider(lookthroughMappingService, chronId);			
		dataProvider.setNumberPerPage(IPageConstants.RECORDS_PER_PAGE);
		
		IDataTable<LookthroughMappingVO> table = getWidgetFactory().<LookthroughMappingVO>createTable("lookthroughMappingTable")
			.setDataProvider(dataProvider);
		table.setRowsPerPage(IPageConstants.RECORDS_PER_PAGE);
	
		table.createColumn(getStringResource("lookthroughPortfolioAccountTitle", this), null)
		.setComponent(new DataTablePropertyLabelComponent<LookthroughMappingVO>(LookthroughMappingVO.PROPERTY_LOOKTHROUGH_PORTFOLIO_ACCOUNT_CODE))
		.setSortProperty(LookthroughMappingVO.PROPERTY_LOOKTHROUGH_PORTFOLIO_ACCOUNT_CODE);

		table.createColumn(getStringResource("mappedPortfolioAccountTitle", this), null)
		.setComponent(new DataTablePropertyLabelComponent<LookthroughMappingVO>(LookthroughMappingVO.PROPERTY_MAPPED_PORTFOLIO_ACCOUNT_CODE))
		.setSortProperty(LookthroughMappingVO.PROPERTY_MAPPED_PORTFOLIO_ACCOUNT_CODE);

		table.createColumn(getStringResource("mappedPortfolioAccountNameTitle", this), null)
		.setComponent(new DataTablePropertyLabelComponent<LookthroughMappingVO>(LookthroughMappingVO.PROPERTY_MAPPED_PORTFOLIO_ACCOUNT_NAME))
		.setSortProperty(LookthroughMappingVO.PROPERTY_MAPPED_PORTFOLIO_ACCOUNT_NAME);

		if (chronId == null) {
			table.createColumn(getStringResource("actionTitle", this), null)			
			.setComponent(new LookthroughMappingActionComponent(lookthroughMappingService, getFeedbackHandler()));
		}

		return table;
	}

	@Override
	protected String getPageName() {
		return IPageConstants.LOOKTHROUGH_MAPPING_LIST_PAGE;
	}

}
