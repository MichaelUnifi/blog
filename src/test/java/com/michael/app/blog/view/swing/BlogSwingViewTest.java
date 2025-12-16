package com.michael.app.blog.view.swing;

import org.junit.Test;

import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.michael.app.blog.controller.BlogController;
import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;

@RunWith(GUITestRunner.class)
public class BlogSwingViewTest extends AssertJSwingJUnitTestCase{
	
	@Mock
	private BlogController blogController;
	private AutoCloseable closeable;
	
	private FrameFixture window;
	
	private BlogSwingView blogView;

	private Article article1;
	private Article article2;
	private String content;
	private String title;
	private String id1;
	private String id2;
	private Tag tag;
	private Set<String> tagLabels;

	@Override
	protected void onSetUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		id1 = "000000000000000000000000";
		id2 = "000000000000000000000000";
		title = "Parmesan eggplants";
		content = "I like them";
		article1 = new Article(id1, title, content);
		article2 = new Article(id2, "Fettuccine alfredo", "America thinks it's something special");
		tag = new Tag("cooking");
		tagLabels = new HashSet<String>();
		GuiActionRunner.execute(() -> {
			blogView = new BlogSwingView();
			blogView.setBlogController(blogController);
			return blogView;
			});
			window = new FrameFixture(robot(), blogView);
			window.show();
	}
	
	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}
	
	@Test @GUITest
	public void testControlsInitialState() {
		window.label(JLabelMatcher.withText("Title"));
		window.textBox("TitleTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("Content"));
		window.button(JButtonMatcher.withText("Save")).requireDisabled();
		window.button(JButtonMatcher.withText("Delete")).requireDisabled();
		window.textBox("ContentTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("Tags"));
		window.button(JButtonMatcher.withText("Filter")).requireDisabled();
		window.button(JButtonMatcher.withText("Reset")).requireEnabled();
		window.textBox("FilterTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("Add new/Remove from selected"));
		window.textBox("FilterTextBox").requireEnabled();
		window.button(JButtonMatcher.withText("Add")).requireDisabled();
		window.button(JButtonMatcher.withText("Remove")).requireDisabled();
		window.list("articleList");
		window.list("tagList");
		window.label("errorMessageLabel").requireText(" ");
	}
	
	@Test
	public void startShouldCallControllerAllArticlesAndSetShowingToTrue() {
		GuiActionRunner.execute(() -> blogView.start());
		verify(blogController).allArticles();
		window.requireVisible();
	}
	
	@Test @GUITest
	public void testWhenTitleAndContentAreNotEmptySaveIsEnabled() {
		window.textBox("TitleTextBox").enterText("test");
		window.textBox("ContentTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Save")).requireEnabled();
	}

	@Test @GUITest
	public void testWhenEitherTitleOrContentAreBlankThenSaveButtonShouldBeDisabled() {
		JTextComponentFixture titleTextBox = window.textBox("TitleTextBox");
		JTextComponentFixture contentTextBox = window.textBox("ContentTextBox");
		
		titleTextBox.enterText("test");
		contentTextBox.enterText(" ");
		window.button(JButtonMatcher.withText("Save")).requireDisabled();
		
		titleTextBox.setText("");
		contentTextBox.setText("");
		
		titleTextBox.enterText(" ");
		contentTextBox.enterText("test");
		window.button(JButtonMatcher.withText("Save")).requireDisabled();
	}
	
	@Test @GUITest
	public void testDeleteButtonShouldBeDisabledWhenNoArticleIsSelected() {
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete"));
		window.button(JButtonMatcher.withText("Delete"));
		deleteButton.requireDisabled();
	}
	
	@Test @GUITest
	public void testDeleteButtonShouldBeEnabledWhenAnArticleIsSelected() {
		GuiActionRunner.execute(() -> blogView.getListArticlesModel().addElement(article1));
		window.list("articleList").selectItem(0);
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete"));
		window.button(JButtonMatcher.withText("Delete"));
		deleteButton.requireEnabled();
	}
	
	@Test @GUITest
	public void testAddButtonShouldBeEnabledWhenTagTextIsNotEmpty() {
		window.textBox("TagTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Add")).requireEnabled();
	}
	
	@Test @GUITest
	public void testAddButtonShouldBeDisabledWhenTagTextIsEmptyorBlank() {
		window.textBox("TagTextBox").enterText("test");
		window.textBox("TagTextBox").deleteText();
		window.button(JButtonMatcher.withText("Add")).requireDisabled();
		window.textBox("TagTextBox").enterText(" ");
		window.button(JButtonMatcher.withText("Add")).requireDisabled();
	}
	
	@Test @GUITest
	public void testRemoveButtonShouldBeEnabledWhenATagIsSelected() {
		GuiActionRunner.execute(() -> blogView.getListTagsModel().addElement(tag));
		window.list("tagList").selectItem(0);
		window.button(JButtonMatcher.withText("Remove")).requireEnabled();
	}
	
	@Test @GUITest
	public void testFilterButtonShouldBeDisabledWhenFilterTextIsEmptyOrBlank() {
		window.textBox("FilterTextBox").setText("");
		window.button(JButtonMatcher.withText("Filter")).requireDisabled();
		window.textBox("FilterTextBox").enterText(" ");
		window.button(JButtonMatcher.withText("Filter")).requireDisabled();
	}
	@Test @GUITest
	public void testFilterButtonShouldBeEnabledWhenFilterTextIsNotEmptyOrBlank() {
		window.textBox("FilterTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Filter")).requireEnabled();
	}
	
	@Test @GUITest
	public void testWhenArticleIsSelectedTitleContentAndTagsAreShown() {
		article1.addTag(tag);
		GuiActionRunner.execute(() -> blogView.getListArticlesModel().addElement(article1));
		window.list("articleList").selectItem(0);
		window.textBox("TitleTextBox").requireText(title);
		window.textBox("ContentTextBox").requireText(content);
		String[] listContents = window.list("tagList").contents();
		assertThat(listContents).containsExactly(tag.toString());
	}
	
	@Test @GUITest
	public void testWhenATagIsSelectedTagLabelIsShown() {
		GuiActionRunner.execute(() -> blogView.getListTagsModel().addElement(tag));
		window.list("tagList").selectItem(0);
		window.textBox("TagTextBox").requireText("cooking");
	}
	
	@Test
	public void testShowAllArticlesClearsAndAddsArticlesDescriptionsToTheList() {
		String id3 = "000000000000000000000002";
		GuiActionRunner.execute(() -> blogView.getListArticlesModel().addElement(
			new Article(id3, "Steak", "My favourite")
		));
		GuiActionRunner.execute(() ->
			blogView.showAllArticles(Arrays.asList(article1, article2))
		);
		String[] listContents = window.list("articleList").contents();
		assertThat(listContents).containsExactly(article1.toString(), article2.toString());
	}
	
	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {
		GuiActionRunner.execute(
			() -> blogView.showError("error message")
		);
		window.label("errorMessageLabel").requireText("error message");
	}
	
	@Test
	public void testArticleAddedAddsTheArticleToTheListAndResetsTheErrorLabel() {
		GuiActionRunner.execute(() -> blogView.getListArticlesModel().addElement(article1));
		GuiActionRunner.execute(() -> blogView.articleAdded(article2));
		String[] listContents = window.list("articleList").contents();
		assertThat(listContents).containsExactly(article1.toString(), article2.toString());
		window.label("errorMessageLabel").requireText(" ");
	}
	
	@Test
	public void testArticleUpdatedReplacesTheOriginalArticleInTheListAndResetsTheErrorLabel() {
		Article article = new Article(id1, title, content);
		GuiActionRunner.execute(() -> blogView.getListArticlesModel().addElement(article));
		window.list("articleList").selectItem(0);
		Article updatedArticle = new Article(id1, title, "Now I don't like them");
		GuiActionRunner.execute(() -> blogView.articleUpdated(updatedArticle));
		String[] listContents = window.list("articleList").contents();
		assertThat(listContents).containsExactly(updatedArticle.toString());
		window.label("errorMessageLabel").requireText(" ");
	}
	
	@Test
	public void testArticleDeletedRemovesTheArticleInTheListAndResetsTheErrorLabel() {
		GuiActionRunner.execute(() -> blogView.getListArticlesModel().addElement(article1));
		GuiActionRunner.execute(() -> blogView.getListArticlesModel().addElement(article2));
		window.list("articleList").selectItem(0);
		GuiActionRunner.execute(() -> blogView.articleDeleted());
		String[] listContents = window.list("articleList").contents();
		assertThat(listContents).containsExactly(article2.toString());
		window.label("errorMessageLabel").requireText(" ");
	}
	
	@Test
	public void testAddedTagAddsTheTagToTheListAndResetsTheErrorLabel() {
		window.textBox("TagTextBox").enterText("test");
		GuiActionRunner.execute(() -> blogView.addedTag(tag));
		String[] listContents = window.list("tagList").contents();
		assertThat(listContents).containsExactly(tag.toString());
		window.label("errorMessageLabel").requireText(" ");
		window.textBox("TagTextBox").requireText("");
		window.button(JButtonMatcher.withText("Add")).requireDisabled();
		window.button(JButtonMatcher.withText("Remove")).requireDisabled();
	}
	
	@Test
	public void testAddedTagResetsTheTagTextboxAndButtonsAndSelection() {
		window.textBox("TagTextBox").enterText("test");
		GuiActionRunner.execute(() -> blogView.getListTagsModel().addElement(tag));
		window.list("tagList").selectItem(0);
		GuiActionRunner.execute(() -> blogView.addedTag(new Tag("test")));
		window.textBox("TagTextBox").requireText("");
		window.list("tagList").requireNoSelection();
		window.button((JButtonMatcher.withText("Add"))).requireDisabled();
		window.button((JButtonMatcher.withText("Remove"))).requireDisabled();
	}
	
	@Test
	public void testAddedTagShouldCheckSaveButton() {
		window.textBox("TagTextBox").enterText("test");
		window.textBox("TitleTextBox").setText("test");
		window.textBox("ContentTextBox").setText("test");
		GuiActionRunner.execute(() -> blogView.addedTag(tag));
		window.button((JButtonMatcher.withText("Save"))).requireEnabled();
	}
	
	@Test
	public void testRemovedTagRemovesTheTagInTheListAndResetsTheErrorLabelAndTagSelection() {
		GuiActionRunner.execute(() -> blogView.getListTagsModel().addElement(new Tag("test")));
		GuiActionRunner.execute(() -> blogView.getListTagsModel().addElement(tag));
		window.list("tagList").selectItem(0);
		GuiActionRunner.execute(() -> blogView.removedTag());
		String[] listContents = window.list("tagList").contents();
		assertThat(listContents).containsExactly(tag.toString());
		window.label("errorMessageLabel").requireText(" ");
		window.list("tagList").requireNoSelection();
	}
	
	@Test
	public void testRemovedTagShouldCheckSaveButton() {
		GuiActionRunner.execute(() -> blogView.getListTagsModel().addElement(tag));
		window.list("tagList").selectItem(0);
		window.textBox("TitleTextBox").setText("test");
		window.textBox("ContentTextBox").setText("test");
		GuiActionRunner.execute(() -> blogView.removedTag());
		window.button((JButtonMatcher.withText("Save"))).requireEnabled();
	}
	
	@Test
	public void testResetButtonShouldResetAllComponents() {
		GuiActionRunner.execute(() -> blogView.getListArticlesModel().addElement(article1));
		window.list("articleList").selectItem(0);
		GuiActionRunner.execute(() -> blogView.getListTagsModel().addElement(tag));
		window.list("tagList").selectItem(0);
		window.textBox("FilterTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Reset")).click();
		window.textBox("TitleTextBox").requireEmpty();
		window.textBox("ContentTextBox").requireEmpty();
		window.textBox("TagTextBox").requireEmpty();
		window.textBox("FilterTextBox").requireEmpty();
		window.list("articleList").requireNoSelection();
		window.list("tagList").requireNoSelection();
		window.button(JButtonMatcher.withText("Save")).requireDisabled();
		window.button(JButtonMatcher.withText("Delete")).requireDisabled();
		window.button(JButtonMatcher.withText("Add")).requireDisabled();
		window.button(JButtonMatcher.withText("Remove")).requireDisabled();
		window.button(JButtonMatcher.withText("Filter")).requireDisabled();
	}
	
	@Test
	public void testFilterButtonShouldResetAllComponentsExceptFilterTextbox() {
		GuiActionRunner.execute(() -> blogView.getListArticlesModel().addElement(article1));
		window.list("articleList").selectItem(0);
		GuiActionRunner.execute(() -> blogView.getListTagsModel().addElement(tag));
		window.list("tagList").selectItem(0);
		window.textBox("FilterTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Filter")).click();
		window.textBox("TitleTextBox").requireEmpty();
		window.textBox("ContentTextBox").requireEmpty();
		window.textBox("TagTextBox").requireEmpty();
		window.textBox("FilterTextBox").requireText("test");
		window.list("articleList").requireNoSelection();
		window.list("tagList").requireNoSelection();
		window.button(JButtonMatcher.withText("Save")).requireDisabled();
		window.button(JButtonMatcher.withText("Delete")).requireDisabled();
		window.button(JButtonMatcher.withText("Add")).requireDisabled();
		window.button(JButtonMatcher.withText("Remove")).requireDisabled();
		window.button(JButtonMatcher.withText("Filter")).requireEnabled();
	}
	
	@Test
	public void testArticleAddedShouldResetAllComponentsExceptFilterComponentsOnSuccessfulSave() {
		GuiActionRunner.execute(() -> blogView.getListTagsModel().addElement(tag));
		window.list("tagList").selectItem(0);
		window.textBox("TitleTextBox").setText("");
		window.textBox("TitleTextBox").enterText("test");
		GuiActionRunner.execute(() -> blogView.articleAdded(article2));
		window.textBox("TitleTextBox").requireEmpty();
		window.textBox("ContentTextBox").requireEmpty();
		window.list("articleList").requireNoSelection();
		window.list("tagList").requireNoSelection();
		window.button(JButtonMatcher.withText("Save")).requireDisabled();
		window.button(JButtonMatcher.withText("Delete")).requireDisabled();
		window.button(JButtonMatcher.withText("Add")).requireDisabled();
		window.button(JButtonMatcher.withText("Remove")).requireDisabled();
	}
	
	@Test
	public void testArticleUpdatedShouldResetAllComponentsExceptFilterComponentsOnSuccessfulUpdate() {
		GuiActionRunner.execute(() -> blogView.getListArticlesModel().addElement(article1));
		window.list("articleList").selectItem(0);
		GuiActionRunner.execute(() -> blogView.getListTagsModel().addElement(tag));
		window.list("tagList").selectItem(0);
		window.textBox("TitleTextBox").setText("");
		window.textBox("TitleTextBox").enterText("test");
		GuiActionRunner.execute(() -> blogView.articleUpdated(article1));
		window.textBox("TitleTextBox").requireEmpty();
		window.textBox("ContentTextBox").requireEmpty();
		window.list("articleList").requireNoSelection();
		window.list("tagList").requireNoSelection();
		window.button(JButtonMatcher.withText("Save")).requireDisabled();
		window.button(JButtonMatcher.withText("Delete")).requireDisabled();
		window.button(JButtonMatcher.withText("Add")).requireDisabled();
		window.button(JButtonMatcher.withText("Remove")).requireDisabled();
	}
	
	@Test
	public void testArticleDeletedShouldResetAllComponentsExceptFilterComponentsOnSuccessfulDelete() {
		GuiActionRunner.execute(() -> blogView.getListArticlesModel().addElement(article1));
		window.list("articleList").selectItem(0);
		GuiActionRunner.execute(() -> blogView.getListTagsModel().addElement(tag));
		window.list("tagList").selectItem(0);
		GuiActionRunner.execute(() -> blogView.articleDeleted());
		window.textBox("TitleTextBox").requireEmpty();
		window.textBox("ContentTextBox").requireEmpty();
		window.list("articleList").requireNoSelection();
		window.list("tagList").requireNoSelection();
		window.button(JButtonMatcher.withText("Save")).requireDisabled();
		window.button(JButtonMatcher.withText("Delete")).requireDisabled();
		window.button(JButtonMatcher.withText("Add")).requireDisabled();
		window.button(JButtonMatcher.withText("Remove")).requireDisabled();
	}
	
	@Test
	public void testSaveButtonShouldDelegateToBlogControllerSaveArticleWhenNoArticleIsSelected() {
		window.textBox("TitleTextBox").enterText("test");
		window.textBox("ContentTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Save")).click();
		verify(blogController).saveArticle("test", "test", tagLabels);
	}
	
	@Test
	public void testSaveButtonShouldDelegateToBlogControllerUpdateArticleWhenAnArticleIsSelected() {
		GuiActionRunner.execute(() -> blogView.getListArticlesModel().addElement(article1));
		window.list("articleList").selectItem(0);
		window.textBox("ContentTextBox").setText("");
		window.textBox("ContentTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Save")).click();
		verify(blogController).updateArticle(id1, title, "test", tagLabels);
	}
	
	@Test
	public void testDeleteButtonShouldDelegateToBlogControllerDeleteArticle() {
		GuiActionRunner.execute(() -> blogView.getListArticlesModel().addElement(article1));
		window.list("articleList").selectItem(0);
		window.button(JButtonMatcher.withText("Delete")).click();
		verify(blogController).deleteArticle(id1);
	}
	
	@Test
	public void testFilterShouldDelegateToBlogControllerAllArticlesWithTag() {
		window.textBox("FilterTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Filter")).click();
		verify(blogController).allArticlesWithTag("test");
	}

	@Test
	public void testResetShouldDelegateToBlogControllerAllArticles() {
		window.button(JButtonMatcher.withText("Reset")).click();
		verify(blogController).allArticles();
	}
	
	@Test
	public void testAddShouldDelegateToBlogControllerTag() {
		window.textBox("TagTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Add")).click();
		verify(blogController).tag("test");
	}
	
	@Test
	public void testRemoveShouldCallArticleRemoved() {
		GuiActionRunner.execute(() -> blogView.getListTagsModel().addElement(tag));
		window.list("tagList").selectItem(0);
		window.button(JButtonMatcher.withText("Remove")).click();
		assertThat(window.list("tagList").contents()).isEmpty();
	}
}
