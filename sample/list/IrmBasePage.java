package ca.cppib.irm.web;


import java.util.Calendar;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ca.cppib.irm.servicesapi.IVersionService;
import ca.cppib.irm.servicesapi.bean.ref.VersionVO;
import ca.cppib.irm.web.authorizer.AnonymousAuthorizer;
import ca.cppib.irm.web.pages.admin.SystemAdministrationPage;
import ca.cppib.irm.web.pages.batch.BatchManagerPage;
import ca.cppib.irm.web.pages.hierarchy.HierarchyMenuPage;
import ca.cppib.irm.web.pages.reports.ReportManagementPage;
import ca.cppib.irm.web.pages.riskmetrics.RiskMetricsMenuPage;
import ca.cppib.irm.web.wicket.feedback.FeedbackHandlerImpl;
import ca.cppib.irm.web.wicket.feedback.IFeedbackHandler;
import ca.cppib.irm.webconstants.IIrmMenuConstants;
import ca.cppib.irm.webutils.IrmClientUserDetails;
import ca.intelliware.fs.spring.security.ISecurityContextHelper;
import ca.intelliware.fs.util.testuuid.FSTestUUIDDiscovery;
import ca.intelliware.fs.wicket.link.SecuredBookmarkablePageLink;
import ca.intelliware.fs.wicket.page.FSWebPage;
import ca.intelliware.fs.wicket.rules.IJavaCodeRule;
import ca.intelliware.fs.wicket.rules.IRulesEngine;
import ca.intelliware.fs.wicket.utils.IWidgetFactory;
import ca.intelliware.fs.wicket.utils.WidgetFactory;
import ca.intelliware.fs.wicket.utils.pagecount.IPageCountUpdater;
import ca.intelliware.fs.wicket.utils.pageinterface.FSPageInterfaceImpl;
import ca.intelliware.fs.wicket.utils.pageinterface.IFSAuthorizer;
import ca.intelliware.fs.wicket.utils.pageinterface.IFSPageInterface;

public abstract class IrmBasePage extends FSWebPage {
	@SpringBean
	private ISecurityContextHelper<IrmClientUserDetails> securityContextHelper;
	
	@SpringBean
	private IVersionService versionService;
	
	private static IWidgetFactory widgetFactory = new WidgetFactory();	
	private static VersionVO cachedVersion;
	private static IFeedbackHandler FEEDBACK_HANDLER = new FeedbackHandlerImpl();
	
	@SpringBean 
	private IPageCountUpdater pageCountUpdater;
	
	@SpringBean(name="RulesEngine")
	private IRulesEngine rulesEngine;
	
	private String titleResourceKey;
	private Link<Void> logoutLink;
	private FeedbackPanel feedbackPanel;
	
	public IrmBasePage(PageParameters pageParameters) {
		super(pageParameters);
		setTitleResourceKey();		
		pageCountUpdater.addPagenameAndCounter(this, getPageName());
		// Construct form and feedback panel and hook them up
		feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		add(feedbackPanel);
	}

	protected void setTitleResourceKey() {
		this.titleResourceKey = "title." + this.getClass().getSimpleName();
	}

	protected VersionVO getVersion() {
		if(cachedVersion==null) {
			cachedVersion = versionService.getVersion();			
		}
		return cachedVersion;		
	}
	
	@Override
	protected void onBeforeRender() {
		if (!hasBeenRendered()) {
			addComponents();
		}
		// Page security
		IFSPageInterface pageInterface = getPageInterface(this);
		if(pageInterface != null && !pageInterface.prepare(getPageParameters()).isAuthorized()) {
			redirectToInterceptPage(new AccessDenied(getPageParameters()));
		}
		super.onBeforeRender();
	}
		
	protected abstract String getPageName();
	
	@SuppressWarnings("serial")
	private void addComponents() {
		add(new StyleSheetReference("stylesheet", new ResourceReference(IrmWebWicketApplication.class, "IrmWeb.css")));
		add(new StyleSheetReference("stylesheetCPP", new ResourceReference(IrmWebWicketApplication.class, "cppstyles.css")));
		add(getFavIconLink());
		add(new Label("pageTitle", getPageTitle()));
		add(new Label("titleDiv", getTitle()));
		ExternalLink versionLink = new ExternalLink("version", "#");
		Label versionNumberLabel = new Label("versionNumber", getVersion().getSoftwareVersion());
		versionNumberLabel.setRenderBodyOnly(true);
		versionLink.add(versionNumberLabel);
		versionLink.add(new Label("buildVersionNumber", getVersion().getSoftwareVersion()));
		versionLink.add(new Label("buildId", getVersion().getBuildId()));
		versionLink.add(new Label("buildLabel", getVersion().getBuildLabel()));
		versionLink.add(new Label("buildDate", getVersion().getBuildDate()));
		add(versionLink);
		add(new Label("userInfo", getUserInfo()));
		add(new Label("copyright", getString("copyright")));
		add(new Label("copyrightYear", Model.of(Calendar.getInstance().get(Calendar.YEAR))));
		
		add(new ExternalLink("companyExternalLink", getString("companyUrl"), getString("companyName")));
		
		BookmarkablePageLink<Login> loginLink = new BookmarkablePageLink<Login>("loginLink", Login.class);
		loginLink.setVisible(!isLoggedIn());
		add(loginLink);
		
		logoutLink = new Link<Void>("logoutLink") {
			@Override
			public void onClick() {
				getSecurityContextHelper().logout();
				AuthenticatedWebSession.get().signOut();
				setResponsePage(Login.class);
			}
		};
		add(logoutLink);
		logoutLink.setVisible(isLoggedIn());
		
		addMenu();

		StringBuilder testUUIDString = new StringBuilder();
		if(FSTestUUIDDiscovery.isThreadInTest()) {
			testUUIDString.append("Test UUID:" + FSTestUUIDDiscovery.getThreadTestUUID());
		}
		Label testUUIDLabel = new Label("testUUIDLabel", testUUIDString.toString());
		testUUIDLabel.setVisible(FSTestUUIDDiscovery.isThreadInTest());
		add(testUUIDLabel);

	}
	
	private IModel<String> getPageTitle() {
		return new StringResourceModel("pageTitle", this, getTitle());
	}
	
	private IModel<String> getUserInfo() {
		IrmClientUserDetails userDetails = this.getUserDetails();
		if( userDetails != null){
			return new StringResourceModel("userInfo", this, new Model<IrmClientUserDetails>(userDetails));
		}
		return new StringResourceModel("notLoggedIn", this, null, "notLoggedIn");
	}
	
	private void addMenu() {
		MarkupContainer menuContainer = new WebMarkupContainer("menuContainer");
		add(menuContainer);
		
		RepeatingView menu = new RepeatingView("menuItem");
		
		addMenu(menu, Home.getPageInterface().create().prepare().createBookmarkablePageLink(IIrmMenuConstants.HOME_PAGE_MENU_ITEM, getStringResource("homePageMenuItem", null)));
		addMenu(menu, BatchManagerPage.getPageInterface().create().prepare().createBookmarkablePageLink(IIrmMenuConstants.BATCH_PAGE_ITEM, getStringResource("batchManagerPage", null)));
		addMenu(menu, ReportManagementPage.getPageInterface().create().prepare().createBookmarkablePageLink(IIrmMenuConstants.REPORT_MANAGEMENT_ITEM, getStringResource("reportMenuItem", null)));
		addMenu(menu, HierarchyMenuPage.getPageInterface().create().prepare().createBookmarkablePageLink(IIrmMenuConstants.HIERARCHY_MENU_ITEM, getStringResource("hierarchyPageMenuItem", null)));
		addMenu(menu, RiskMetricsMenuPage.getPageInterface().create().prepare().createBookmarkablePageLink(IIrmMenuConstants.RISK_METRICS_MENU_ITEM, getStringResource("riskMetricsMenuItem", null)));		
		addMenu(menu, SystemAdministrationPage.getPageInterface().create().prepare().createBookmarkablePageLink(IIrmMenuConstants.SYSTEM_ADMINISTRATION_MENU_ITEM, getStringResource("systemAdministrationMenuItem", null)));
		menuContainer.setVisible(isLoggedIn());
		menuContainer.add(menu);
	}

	private void addMenu(RepeatingView menu, SecuredBookmarkablePageLink link) {
		menu.add(link);
	}
	
	protected StringResourceModel getTitle() {
		String resourceKey = getTitleResourceKey();
		return new StringResourceModel(resourceKey, this, null, resourceKey);
	}
	
	public ExternalLink getFavIconLink() {
		String url = urlFor(new ResourceReference(IrmWebWicketApplication.class, "favicon.ico")).toString();
		ExternalLink link = new ExternalLink("favicon", url);
		link.add(new SimpleAttributeModifier("type", "image/x-icon"));
		link.add(new SimpleAttributeModifier("rel", "shortcut icon"));
		return link;
	}

	public void setLocale(Locale locale)
	{
		if (locale != null)
		{
			getSession().setLocale(locale);
		}
	}

	public IrmClientUserDetails getUserDetails() {
		return securityContextHelper.getUserDetails();
	}
	
	public void logout() {
		IrmAuthenticatedWebSession session = (IrmAuthenticatedWebSession)getSession();
		session.signOut();
		session.invalidate();
		throw new RestartResponseAtInterceptPageException(Home.class);					
	}
	
	public String getAssetUrl(HttpServletRequest httpServletRequest, Long assetId) {
		//http://localhost:8080/IrmWeb/asset?assetId=${assetId}
		String assetUrl = "http://${host}:${port}/IrmWeb/asset?assetId=${assetId}";
		assetUrl = StringUtils.replace(assetUrl, "${assetId}", "" + assetId);
		assetUrl = StringUtils.replace(assetUrl, "${host}", httpServletRequest.getServerName());
		assetUrl = StringUtils.replace(assetUrl, "${port}", "" + httpServletRequest.getServerPort());
		return assetUrl;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void onAfterRender() {
		super.onAfterRender();
		if( rulesEngine != null && rulesEngine.getJavaRules().size() > 0){
			visitChildren(Component.class, new IVisitor() {
				@Override
				public Object component(Component component) {
					for(IJavaCodeRule rule : rulesEngine.getJavaRules()){
						rule.applyRule(component, getPageName());
					}
					return IVisitor.CONTINUE_TRAVERSAL;
				}
			});
		}
	}

	protected Link<Void> getLogoutLink() {
		return logoutLink;
	}
	
	protected boolean isLoggedIn() {
		return getSecurityContextHelper().getUserDetails() != null; 
	}

	public ISecurityContextHelper<IrmClientUserDetails> getSecurityContextHelper() {
		return securityContextHelper;
	}

	protected StringResourceModel getStringResource(String key, Component component) {
		return getWidgetFactory().getStringResource(key, component);
	}

	public IWidgetFactory getWidgetFactory() {
		return widgetFactory;
	}

	protected static <P extends WebPage, T> IFSPageInterface<P,T> createPageInterface(Class<P> pageClass, 
			Class<T> parameterClass, IFSAuthorizer<T> authorizer) {
		if(authorizer == null) {
			authorizer = new AnonymousAuthorizer<T>();
		}
		return new FSPageInterfaceImpl<P, T>(pageClass, parameterClass, authorizer);
	}

	protected final HttpServletRequest getHttpRequest() {
		return ((WebRequest)RequestCycle.get().getRequest()).getHttpServletRequest();
	}

	protected String getTitleResourceKey() {
		return titleResourceKey;
	}

	protected void setTitleResourceKey(String titleResourceKey) {
		this.titleResourceKey = titleResourceKey;
	}

    public static IFeedbackHandler getFeedbackHandler() {
    	return FEEDBACK_HANDLER;
    }
    
	protected FeedbackPanel getFeedbackPanel() {
		return feedbackPanel;
	}
}
