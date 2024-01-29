package com.daimler.data.controller;

import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.daimler.data.api.workspace.recipe.CodeServerRecipeApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.dto.workspace.recipe.RecipeVO;
import com.daimler.data.service.workspace.RecipeService;
import com.daimler.data.service.workspace.WorkspaceService;
import com.daimler.data.dto.workspace.recipe.InitializeRecipeVo;
import com.daimler.data.dto.workspace.recipe.RecipeCollectionVO;
import com.daimler.data.controller.exceptions.GenericMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Recipe API", tags = { "code-server-recipe" })
@RequestMapping("/api")
@Slf4j
public class RecipeController implements CodeServerRecipeApi {

     @Autowired
	 private RecipeService service;

	 @Autowired
	 private UserStore userStore;

	@Override
	@ApiOperation(value = "Initialize/Create recipe for user in code-server-recipe.", nickname = "createRecipe", notes = "Create recipe for user in code-server with given password", response = RecipeVO.class, tags = {
			"code-server-recipe", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = RecipeVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/recipeDetails", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<InitializeRecipeVo> createRecipe(
			@ApiParam(value = "Request Body that contains data required for intialize code server workbench for user", required = true) @Valid @RequestBody RecipeVO recipeRequestVO) {
				
		String recipeName = recipeRequestVO.getRecipeName() != null ? recipeRequestVO.getRecipeName() : null;
		//RecipeVO vo = service.getByRecipeName(recipeName);
		InitializeRecipeVo responseMessage = new InitializeRecipeVo();
		String name = service.getByRecipeName(recipeName)!= null ? service.getByRecipeName(recipeName).getRecipeName() : null;
		if (name == null) {
			RecipeVO recipeVO = service.createRecipe(recipeRequestVO);
			if (Objects.nonNull(recipeVO)) {
				responseMessage.setData(recipeVO);
				responseMessage.setSuccess("SUCCESS");
				log.info("Recipe is created sucessfully with name : "+recipeName);
				return new ResponseEntity<>(responseMessage, HttpStatus.CREATED);
			} else {
				responseMessage.setData(null);
				responseMessage.setSuccess("FAILED");
				log.info("Failed while creating recipe with recipeName : "+recipeName);
				return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
			}
		} else {
			responseMessage.setData(null);
			responseMessage.setSuccess("CONFLICT");
			log.info("workspace {} already exists for User {} with name: {} ", recipeName);
			return new ResponseEntity<>(responseMessage, HttpStatus.CONFLICT);
		}
	}

	@Override
	@ApiOperation(value = "Get all recipe managment workspaces for the user.", nickname = "getAllRecipes", notes = "Get all codeServer recipe managment workspaces for the user.", response = RecipeCollectionVO.class, tags = {
			"code-server-recipe", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = RecipeCollectionVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/recipeDetails", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<RecipeCollectionVO> getAllRecipes(
			@ApiParam(value = "page number from which listing of workspaces should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of workspaces, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
		RecipeCollectionVO recipeCollectionVO = new RecipeCollectionVO();

		if (offset == null) {
			offset = 0;
		}
		if (limit == null) {
			limit = 0;
		}
		if (userStore.getUserInfo().hasCodespaceAdminAccess()) {
			List<RecipeVO> allRecipes = service.getAllRecipes(offset, limit);
			if (Objects.nonNull(allRecipes)) {
				for (RecipeVO recipe : allRecipes) {
					recipeCollectionVO.addDataItem(recipe);
				}
				recipeCollectionVO.setCount(allRecipes.size());
				recipeCollectionVO.setSuccess("SUCCESS");
				return new ResponseEntity<>(recipeCollectionVO, HttpStatus.OK);
			} else {
				recipeCollectionVO.setData(null);
				recipeCollectionVO.setCount(null);
				recipeCollectionVO.setSuccess("FAILED");
				log.info("Failed to fetch all the recipe details for use "+userStore.getUserInfo().getId());
				return new ResponseEntity<>(recipeCollectionVO, HttpStatus.NO_CONTENT);
			}

		} else {
			recipeCollectionVO.setData(null);
			recipeCollectionVO.setCount(null);
			recipeCollectionVO.setSuccess("CONFLICT");
			log.info(" user is unauthorized to access codespace" + userStore.getUserInfo().getId());
			return new ResponseEntity<>(recipeCollectionVO, HttpStatus.UNAUTHORIZED);
		}
	}

	@Override
	@ApiOperation(value = "Get workspace recipe details for a given Id.", nickname = "getByRecipeName", notes = "Get workspace recipe details for a given Id.", response = RecipeVO.class, tags = {
			"code-server-recipe", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = RecipeVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/recipeDetails/{recipeName}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<InitializeRecipeVo> getByRecipeName(
			@ApiParam(value = "Workspace ID to be fetched", required = true) @PathVariable("recipeName") String recipeName) {
		
			InitializeRecipeVo responseMessage = new InitializeRecipeVo();
		if (userStore.getUserInfo().hasCodespaceAdminAccess()) {
			RecipeVO recipeVO = service.getByRecipeName(recipeName);
			if (Objects.nonNull(recipeVO) && Objects.nonNull(recipeVO.getRecipeName())) {
				responseMessage.setSuccess("SUCCESS");
				responseMessage.setData(recipeVO);
				return new ResponseEntity<>(responseMessage, HttpStatus.OK);
			} else {
				responseMessage.setSuccess("FAILED");
				responseMessage.setData(null);
				log.info("No recipe found for given recipeName: {} ", recipeName);
				return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
			}
		} else {

			responseMessage.setData(null);
			responseMessage.setSuccess("UNAUTHORIZED");
			log.info(" user {} is unauthorized to access codespace" + userStore.getUserInfo().getId());
			return new ResponseEntity<>(responseMessage, HttpStatus.UNAUTHORIZED);

		}
	}
    
}
