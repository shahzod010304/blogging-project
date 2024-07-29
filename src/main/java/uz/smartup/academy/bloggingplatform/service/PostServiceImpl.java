package uz.smartup.academy.bloggingplatform.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import uz.smartup.academy.bloggingplatform.dao.CategoryDao;
import uz.smartup.academy.bloggingplatform.dao.PostDao;
import uz.smartup.academy.bloggingplatform.dao.TagDao;
import uz.smartup.academy.bloggingplatform.dao.UserDao;
import uz.smartup.academy.bloggingplatform.dto.*;
import uz.smartup.academy.bloggingplatform.entity.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class PostServiceImpl implements PostService {
    private final PostDao dao;
    private final UserDao userDao;
    private final PostDtoUtil dtoUtil;
    private final CommentDtoUtil commentDtoUtil;
    private final LikeService likeService;
    private final CategoryDao categoryDao;
    private final TagDao tagDao;
    private final CategoryService categoryService;
    private final UserDtoUtil userDtoUtil;


    public PostServiceImpl(PostDao dao, PostDtoUtil dtoUtil, CommentDtoUtil commentDtoUtil, LikeService likeService, PostDtoUtil postDtoUtil, UserDao userDao, CategoryDao categoryDao, TagDao tagDao, CategoryService categoryService, UserDtoUtil userDtoUtil) {
        this.dao = dao;
        this.dtoUtil = dtoUtil;
        this.commentDtoUtil = commentDtoUtil;
        this.likeService = likeService;
        this.userDao = userDao;
        this.categoryDao = categoryDao;
        this.tagDao = tagDao;
        this.categoryService = categoryService;
        this.userDtoUtil = userDtoUtil;
    }

    @Override
    @Transactional
    public void createPost(Post post) {
        dao.save(post);
    }

    @Override
    @Transactional
    public void update(PostDto postDto) {
        Post post = dtoUtil.toEntity(postDto);

        post.setComments(dao.getPostComments(post.getId()));
        post.setAuthor(dao.getAuthorById(post.getId()));
        post.setStatus(dao.findPostStatusById(post.getId()));
        post.setTags(tagDao.getTagsByPostId(post.getId()));
        post.setCreatedAt(dao.getById(post.getId()).getCreatedAt());

        List<CategoryDto> categories = categoryService.getCategoriesByPostId(post.getId());

        if (categories != null) {
            for (CategoryDto category : categories) {
                removeCategoryFromPost(post.getId(), category.getId());
            }
        }

        List<Category> categories1 = new ArrayList<>();

        postDto.getCategories().forEach(categoryId -> {
            categories1.add(categoryDao.findCategoryById(categoryId));
        });

        post.setCategories(categories1);

        dao.update(post);
    }

    @Override
    @Transactional
    public void delete(int postId) {
        List<LikeDTO> likes = likeService.getLikesByPostId(postId);

        for(LikeDTO like : likes)
            likeService.removeLike(like.getUserId(), postId);

        dao.delete(dao.getById(postId));
    }

    @Override
    public PostDto getById(int id) {
        return dtoUtil.toDto(dao.getById(id));
    }

    @Override
    public List<PostDto> getAllPosts() {
        return dtoUtil.toDTOs(dao.getAllPosts());
    }

    @Override
    public UserDTO getAuthorById(int id) {
        return userDtoUtil.toDTO(dao.getAuthorById(id));
    }

    @Override
    public List<PostDto> getPostsByAuthor(int authorId) {
        return dtoUtil.toDTOs(dao.getPostsByAuthor(authorId));
    }

    @Override
    public List<CommentDTO> getPostComments(int id) {
        List<Comment> comments = dao.getPostComments(id);

        return commentDtoUtil.toDTOs(comments);
    }

    @Override
    public List<PostDto> getDraftPost() {
        List<Post> posts = dao.findPostsByStatus(Post.Status.DRAFT);

        return dtoUtil.toDTOs(posts);
    }

    @Override
    public List<PostDto> getPublishedPost() {
        List<Post> posts = dao.findPostsByStatus(Post.Status.PUBLISHED);

        return dtoUtil.toDTOs(posts);
    }

    @Override
    public List<PostDto> getDraftPostsByAuthorId(int authorId) {
        List<Post> posts = dao.findPostByStatusAndAuthorId(Post.Status.DRAFT, authorId);

//        System.out.println(dtoUtil.toDTOs(posts));
        return dtoUtil.toDTOs(posts);
    }

    @Override
    public List<PostDto> getPublishedPostsByAuthorId(int authorId) {
        List<Post> posts = dao.findPostByStatusAndAuthorId(Post.Status.PUBLISHED, authorId);

        System.out.println(dtoUtil.toDTOs(posts));

        return dtoUtil.toDTOs(posts);
    }

    @Override
    public Post getPostWithLikeCount(int postId) {
        Post post = dtoUtil.toEntity(getById(postId));
        long likeCount = likeService.countLikesByPostId(postId);
        post.setLikesCount(likeCount);
        return post;
    }
  
    @Transactional
    @Override
    public void addCommentToPost(int userId, int postId, CommentDTO commentDTO) {
        Post post = dao.getById(postId);

        if (post == null)
            throw new IllegalArgumentException("Post not found with ID: " + postId);

        User user = userDao.getUserById(userId);
        Comment comment = commentDtoUtil.toEntity(commentDTO);
        comment.setAuthor(user);
        comment.setCreatedAt(LocalDateTime.now());
        post.addComments(comment);
        dao.save(post);
    }

    @Override
    @Transactional
    public void switchPostDraftToPublished(int id) {
        Post post = dao.getById(id);

        post.setStatus(Post.Status.PUBLISHED);
        post.setCreatedAt(LocalDateTime.now());

        dao.update(post);
    }

    @Override
    @Transactional
    public void switchPublishedToDraft(int id) {
        Post post = dao.getById(id);

        post.setStatus(Post.Status.DRAFT);

        dao.update(post);
    }

    @Override
    public List<PostDto> getPostsByCategory(String categoryTitle) {
        List<Post> posts = dao.getPostsByCategory(categoryDao.findCategoryByTitle(categoryTitle));
        return posts == null ? null : dtoUtil.toDTOs(posts);
    }

    @Override
    public List<PostDto> getPostsByTag(String tagTitle) {
        List<Post> posts = dao.getPostsByTag(tagDao.findTagByTitle(tagTitle));
        return dtoUtil.toDTOs(posts);
    }

    @Override
    public List<PostDto> searchPosts(String keyword) {
        return dtoUtil.toDTOs(dao.searchPosts(keyword));
    }

    @Override
    @Transactional
    public void removeCategoryFromPost(int postId, int categoryId) {
        Post post = dao.getById(postId);

        post.removeCategory(categoryDao.findCategoryById(categoryId));

        dao.update(post);
    }

    @Override
    @Transactional
    public void addExistCategoriesToPost(int categoryId, int postId) {
        Post post = dao.getById(postId);
        Category category = categoryDao.findCategoryById(categoryId);

        System.out.println(categoryId);

        post.addCategories(category);

        dao.update(post);
    }

}
