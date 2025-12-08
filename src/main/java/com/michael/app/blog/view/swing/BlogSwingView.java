package com.michael.app.blog.view.swing;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.michael.app.blog.controller.BlogController;
import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.michael.app.blog.view.BlogView;

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class BlogSwingView extends JFrame implements BlogView{
	
	private BlogController blogController;

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtTitle;
	private JButton btnFilter;
	private JButton btnSaveArticle;
	private JButton btnDeleteArticle;
	private JTextField txtFilter;
	private JLabel lblContent;
	private JList listArticles;
	private DefaultListModel<Article> listArticlesModel;
	private JLabel lblTags;
	private JTextField txtContent;
	private JList listTags;
	private DefaultListModel<Tag> listTagsModel;
	private JTextField txtTag;
	private JButton btnTag;
	private JButton btnUnTag;
	private JLabel lblError;
	private JLabel lblNewLabel;
	private final JButton btnReset = new JButton("Reset");
	
	public void setBlogController(BlogController blogController) {
		this.blogController = blogController;
		}
	
	DefaultListModel<Article> getListArticlesModel() {
		return listArticlesModel;
	}
	
	DefaultListModel<Tag> getListTagsModel() {
		return listTagsModel;
	}
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BlogSwingView frame = new BlogSwingView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public BlogSwingView() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblTitle = new JLabel("Title");
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.anchor = GridBagConstraints.WEST;
		gbc_lblTitle.gridwidth = 3;
		gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblTitle.gridx = 1;
		gbc_lblTitle.gridy = 1;
		contentPane.add(lblTitle, gbc_lblTitle);
		KeyAdapter btnSaveEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnSaveArticle.setEnabled(
					!txtTitle.getText().trim().isEmpty() &&
					!txtContent.getText().trim().isEmpty()
				);
			}
		};
			
		txtTitle = new JTextField();
		txtTitle.addKeyListener(btnSaveEnabler);
		txtTitle.setName("TitleTextBox");
		GridBagConstraints gbc_txtTitle = new GridBagConstraints();
		gbc_txtTitle.gridwidth = 6;
		gbc_txtTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTitle.insets = new Insets(0, 0, 5, 5);
		gbc_txtTitle.gridx = 4;
		gbc_txtTitle.gridy = 1;
		contentPane.add(txtTitle, gbc_txtTitle);
		txtTitle.setColumns(10);
		
		btnFilter = new JButton("Filter");
		btnFilter.setEnabled(false);
		btnFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				blogController.allArticlesWithTag(txtFilter.getText());
			}
		});
		GridBagConstraints gbc_btnFilter = new GridBagConstraints();
		gbc_btnFilter.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFilter.gridwidth = 2;
		gbc_btnFilter.insets = new Insets(0, 0, 5, 5);
		gbc_btnFilter.gridx = 11;
		gbc_btnFilter.gridy = 1;
		contentPane.add(btnFilter, gbc_btnFilter);
		GridBagConstraints gbc_btnReset = new GridBagConstraints();
		gbc_btnReset.gridwidth = 2;
		gbc_btnReset.insets = new Insets(0, 0, 5, 5);
		gbc_btnReset.gridx = 14;
		gbc_btnReset.gridy = 1;
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				blogController.allArticles();
			}
		});
		contentPane.add(btnReset, gbc_btnReset);
		
		lblContent = new JLabel("Content");
		GridBagConstraints gbc_lblContent = new GridBagConstraints();
		gbc_lblContent.anchor = GridBagConstraints.WEST;
		gbc_lblContent.gridwidth = 3;
		gbc_lblContent.insets = new Insets(0, 0, 5, 5);
		gbc_lblContent.gridx = 1;
		gbc_lblContent.gridy = 2;
		contentPane.add(lblContent, gbc_lblContent);
		
		txtContent = new JTextField();
		txtContent.setName("ContentTextBox");
		txtContent.addKeyListener(btnSaveEnabler);
		GridBagConstraints gbc_txtContent = new GridBagConstraints();
		gbc_txtContent.gridwidth = 6;
		gbc_txtContent.insets = new Insets(0, 0, 5, 5);
		gbc_txtContent.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtContent.gridx = 4;
		gbc_txtContent.gridy = 2;
		contentPane.add(txtContent, gbc_txtContent);
		txtContent.setColumns(10);
		
		txtFilter = new JTextField();
		txtFilter.setName("FilterTextBox");
		txtFilter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnFilter.setEnabled(
						!txtFilter.getText().trim().isEmpty()
					);
				if(txtFilter.getText().isEmpty())
					blogController.allArticles();
			}
		});
		GridBagConstraints gbc_txtFilter = new GridBagConstraints();
		gbc_txtFilter.gridwidth = 5;
		gbc_txtFilter.insets = new Insets(0, 0, 5, 0);
		gbc_txtFilter.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFilter.gridx = 11;
		gbc_txtFilter.gridy = 2;
		contentPane.add(txtFilter, gbc_txtFilter);
		txtFilter.setColumns(10);
		
		btnSaveArticle = new JButton("Save");
		btnSaveArticle.setEnabled(false);
		btnSaveArticle.setName("saveArticle");
		btnSaveArticle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Set<String> tagLabels = IntStream.range(0, listTagsModel.size())
					.mapToObj(listTagsModel::getElementAt)
					.map(Tag::getLabel)
					.collect(Collectors.toSet()
				);
				if(listArticles.getSelectedIndex() != -1)
					blogController.updateArticle(
						listArticlesModel.getElementAt(listArticles.getSelectedIndex()).getId(),
						txtTitle.getText(),
						txtContent.getText(),
						tagLabels);
				else
					blogController.saveArticle(txtTitle.getText(), txtContent.getText(), tagLabels);
			}
		});
		GridBagConstraints gbc_btnSaveArticle = new GridBagConstraints();
		gbc_btnSaveArticle.gridwidth = 4;
		gbc_btnSaveArticle.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSaveArticle.insets = new Insets(0, 0, 5, 5);
		gbc_btnSaveArticle.gridx = 4;
		gbc_btnSaveArticle.gridy = 3;
		contentPane.add(btnSaveArticle, gbc_btnSaveArticle);
		
		btnDeleteArticle = new JButton("Delete");
		btnDeleteArticle.setEnabled(false);
		btnDeleteArticle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				blogController.deleteArticle(
					listArticlesModel.getElementAt(listArticles.getSelectedIndex()).getId()
				);
			}
		});
		GridBagConstraints gbc_btnDeleteArticle = new GridBagConstraints();
		gbc_btnDeleteArticle.gridwidth = 2;
		gbc_btnDeleteArticle.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDeleteArticle.insets = new Insets(0, 0, 5, 5);
		gbc_btnDeleteArticle.gridx = 8;
		gbc_btnDeleteArticle.gridy = 3;
		contentPane.add(btnDeleteArticle, gbc_btnDeleteArticle);
		
		listArticlesModel = new DefaultListModel<>();
		listArticles = new JList<>(listArticlesModel);
		listArticles.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				listTagsModel.removeAllElements();
				int selectedIndex = listArticles.getSelectedIndex();
				if(selectedIndex != -1) {
					btnDeleteArticle.setEnabled(true);
					Article selectedArticle = (Article) listArticles.getModel().getElementAt(listArticles.getSelectedIndex());
					txtTitle.setText(selectedArticle.getTitle());
					txtContent.setText(selectedArticle.getContent());
					selectedArticle.getTags().stream().forEach(listTagsModel::addElement);
				}
				else {
					btnDeleteArticle.setEnabled(false);
					txtTitle.setText("");
					txtContent.setText("");
				}
			}
		});
		listArticles.setName("articleList");
		GridBagConstraints gbc_articleList = new GridBagConstraints();
		gbc_articleList.insets = new Insets(0, 0, 5, 0);
		gbc_articleList.gridwidth = 5;
		gbc_articleList.gridheight = 8;
		gbc_articleList.fill = GridBagConstraints.BOTH;
		gbc_articleList.gridx = 11;
		gbc_articleList.gridy = 3;
		contentPane.add(listArticles, gbc_articleList);
		
		lblTags = new JLabel("Tags");
		GridBagConstraints gbc_lblTags = new GridBagConstraints();
		gbc_lblTags.gridwidth = 6;
		gbc_lblTags.insets = new Insets(0, 0, 5, 5);
		gbc_lblTags.gridx = 0;
		gbc_lblTags.gridy = 5;
		contentPane.add(lblTags, gbc_lblTags);
		
		lblNewLabel = new JLabel("Add new/Remove from selected");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 5;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 6;
		gbc_lblNewLabel.gridy = 5;
		contentPane.add(lblNewLabel, gbc_lblNewLabel);
		
		listTagsModel = new DefaultListModel<>();
		listTags = new JList<>(listTagsModel);
		listTags.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				btnUnTag.setEnabled(listTags.getSelectedIndex() != -1);
			}
		});
		listTags.setName("tagList");
		GridBagConstraints gbc_tagList = new GridBagConstraints();
		gbc_tagList.gridheight = 5;
		gbc_tagList.gridwidth = 6;
		gbc_tagList.insets = new Insets(0, 0, 5, 5);
		gbc_tagList.fill = GridBagConstraints.BOTH;
		gbc_tagList.gridx = 0;
		gbc_tagList.gridy = 6;
		contentPane.add(listTags, gbc_tagList);
		
		txtTag = new JTextField();
		txtTag.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnTag.setEnabled(!txtTag.getText().trim().isEmpty());
				btnUnTag.setEnabled(!txtTag.getText().trim().isEmpty());
			}
		});
		txtTag.setName("TagTextBox");
		GridBagConstraints gbc_txtTag = new GridBagConstraints();
		gbc_txtTag.gridwidth = 5;
		gbc_txtTag.insets = new Insets(0, 0, 5, 5);
		gbc_txtTag.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTag.gridx = 6;
		gbc_txtTag.gridy = 6;
		contentPane.add(txtTag, gbc_txtTag);
		txtTag.setColumns(10);
		
		btnTag = new JButton("Add");
		btnTag.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				blogController.tag(txtTag.getText());
			}
		});
		btnTag.setEnabled(false);
		GridBagConstraints gbc_btnTag = new GridBagConstraints();
		gbc_btnTag.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnTag.gridwidth = 3;
		gbc_btnTag.insets = new Insets(0, 0, 5, 5);
		gbc_btnTag.gridx = 6;
		gbc_btnTag.gridy = 7;
		contentPane.add(btnTag, gbc_btnTag);
		
		btnUnTag = new JButton("Remove");
		btnUnTag.setEnabled(false);
		btnUnTag.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removedTag();
			}
		});
		GridBagConstraints gbc_btnUnTag = new GridBagConstraints();
		gbc_btnUnTag.gridwidth = 2;
		gbc_btnUnTag.insets = new Insets(0, 0, 5, 5);
		gbc_btnUnTag.gridx = 9;
		gbc_btnUnTag.gridy = 7;
		contentPane.add(btnUnTag, gbc_btnUnTag);
		
		lblError = new JLabel(" ");
		lblError.setName("errorMessageLabel");
		GridBagConstraints gbc_lblError = new GridBagConstraints();
		gbc_lblError.fill = GridBagConstraints.VERTICAL;
		gbc_lblError.gridheight = 3;
		gbc_lblError.gridwidth = 5;
		gbc_lblError.insets = new Insets(0, 0, 5, 5);
		gbc_lblError.gridx = 6;
		gbc_lblError.gridy = 8;
		contentPane.add(lblError, gbc_lblError);
	}

	@Override
	public void showAllArticles(List<Article> articles) {
		listArticlesModel.clear();
		articles.stream().forEach(listArticlesModel::addElement);
	}

	@Override
	public void showAllArticlesWithTag(List<Article> articles) {
		listArticlesModel.clear();
		articles.stream().forEach(listArticlesModel::addElement);
	}

	@Override
	public void showError(String errorMessage) {
		lblError.setText(errorMessage);
	}
	
	private void resetErrorLabel() {
		lblError.setText(" ");
	}

	@Override
	public void articleAdded(Article article) {
		listArticlesModel.addElement(article);
		resetErrorLabel();
	}

	@Override
	public void articleUpdated(Article updatedArticle) {
		int index = listArticles.getSelectedIndex();
		listArticlesModel.set(index, updatedArticle);
		resetErrorLabel();
	}

	@Override
	public void articleDeleted() {
		listArticlesModel.removeElement(listArticles.getSelectedValue());
		resetErrorLabel();
	}

	@Override
	public void addedTag(Tag tag) {
		listTagsModel.addElement(tag);
		resetErrorLabel();
	}

	@Override
	public void removedTag() {
		listTagsModel.remove(listTags.getSelectedIndex());
		resetErrorLabel();
	}

}
