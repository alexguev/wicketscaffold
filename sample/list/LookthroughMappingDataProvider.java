package ca.cppib.irm.web.pages.lookthrough;

import java.util.List;

import ca.cppib.irm.servicesapi.lookthrough.ILookthroughMappingService;
import ca.cppib.irm.servicesapi.lookthrough.LookthroughMappingVO;
import ca.cppib.irm.webconstants.IPageConstants;
import ca.intelliware.fs.wicket.utils.dataprovider.FSSortableDataProvider;

public class LookthroughMappingDataProvider extends FSSortableDataProvider<LookthroughMappingVO> {
	private ILookthroughMappingService lookthroughMappingService;
	private Long chronId;
	
	public LookthroughMappingDataProvider(ILookthroughMappingService lookthroughMappingService, Long chronId) {
		super(LookthroughMappingVO.PROPERTY_LOOKTHROUGH_PORTFOLIO_ACCOUNT_CODE, false);
		
		this.lookthroughMappingService = lookthroughMappingService;
		this.chronId = chronId;
		setNumberPerPage(IPageConstants.RECORDS_PER_PAGE);
	}

	@Override
	public List<LookthroughMappingVO> getPagedList(int first, int numberPerPage, String sortProperty, boolean isAscending) {
		return lookthroughMappingService.pageLookthroughMappings(first, numberPerPage, sortProperty, isAscending, chronId);
	}

	@Override
	public int getListSize() {
		return lookthroughMappingService.countLookthroughMappings(chronId);
	}
}
