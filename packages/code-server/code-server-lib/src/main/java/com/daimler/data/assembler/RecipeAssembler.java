package com.daimler.data.assembler;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.catalina.User;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;
import com.daimler.data.dto.workspace.recipe.RecipeVO.OSNameEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.RecipeIdEnum;
import com.daimler.data.dto.workspace.UserInfoVO;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.json.CodeServerRecipe;
import com.daimler.data.db.json.RecipeSoftware;
import com.daimler.data.db.json.UserInfo;
import com.daimler.data.dto.workspace.recipe.RecipeVO;
import com.daimler.data.dto.workspace.recipe.RecipeSoftwareVO;
import com.daimler.data.db.json.CodeServerRecipeLov;
import com.daimler.data.dto.workspace.recipe.RecipeLovVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RecipeAssembler implements GenericAssembler<RecipeVO, CodeServerRecipeNsql> {

	@Override
	public RecipeVO toVo(CodeServerRecipeNsql entity) {
		// TODO Auto-generated method stub
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		RecipeVO recipeVo = new RecipeVO();
			if (Objects.nonNull(entity) && Objects.nonNull(entity.getData())) {
				CodeServerRecipe recipe = entity.getData();
				BeanUtils.copyProperties(recipe, recipeVo);
				recipeVo.setId(entity.getId());
				if (recipe.getSoftware() != null) {
					recipeVo.setSoftware(recipe.getSoftware());
				}
				// List<RecipeSoftware> softwares = recipe.getSoftware();
				// List<RecipeSoftwareVO> softwareVos = new ArrayList<>();
				// if (Objects.nonNull(softwares)) {
				// 	for (RecipeSoftware software : softwares) {
				// 		RecipeSoftwareVO softwareVo = new RecipeSoftwareVO();
				// 		BeanUtils.copyProperties(software, softwareVo);
				// 		softwareVos.add(softwareVo);
				// 	}
				// 	recipeVo.setSoftware(softwareVos);
				// }
				UserInfoVO userInfoVo = new UserInfoVO();
				UserInfo userInfo = recipe.getCreatedBy();
				if (Objects.nonNull(userInfo)) {
					BeanUtils.copyProperties(userInfo, userInfoVo);
					recipeVo.setCreatedBy(userInfoVo);
				}
				if(recipe.getIsPublic()!= null)
				{
					recipeVo.setIsPublic(recipe.getIsPublic());
				}
				else
				{
					recipeVo.setIsPublic(true);
				}
				if (recipe.getOSName() != null) {
					recipeVo.setOSName(OSNameEnum.fromValue(recipe.getOSName()));
				}
				if (recipe.getPlugins() != null) {
					recipeVo.setPlugins(recipe.getPlugins());
				}
				List<UserInfoVO> users = new ArrayList<>();
				List<UserInfo> userDetails = recipe.getUsers();
				if(recipe.getIsPublic())
				{
					users= new ArrayList<UserInfoVO>();
				}
				else
				{
					users = userDetails.stream().map(n->this.toUserInfoVO(n)).collect(Collectors.toList());
				}
				// if(userDetails.size()>0)
				// {
				// 	users = userDetails.stream().map(n->this.toUserInfoVO(n)).collect(Collectors.toList());
				// }
				// else
				// {
				// 	users= new ArrayList<UserInfoVO>();
				// }
				recipeVo.setUsers(users);
			}
		return recipeVo;
	}

	@Override
	public CodeServerRecipeNsql toEntity(RecipeVO vo) {
		// TODO Auto-generated method stub
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		CodeServerRecipeNsql entity = new CodeServerRecipeNsql();
		CodeServerRecipe recipeData = new CodeServerRecipe();
		if (Objects.nonNull(vo)) {
			BeanUtils.copyProperties(vo, recipeData);
			recipeData.setId(vo.getId());
			// List<RecipeSoftwareVO> softwares = vo.getSoftware();
			// List<RecipeSoftware> softwareData = new ArrayList<>();
			// if (Objects.nonNull(softwares)) {
			// 	for (RecipeSoftwareVO software : softwares) {
			// 		RecipeSoftware softwareVo = new RecipeSoftware();
			// 		BeanUtils.copyProperties(software, softwareVo);
			// 		softwareData.add(softwareVo);
			// 	}
			// 	recipeData.setSoftware(softwareData);
			// }
			if (vo.getSoftware() != null) {
				recipeData.setSoftware(vo.getSoftware());
			}
			UserInfo userInfo = new UserInfo();
			UserInfoVO userInfoVo = vo.getCreatedBy();
			if (Objects.nonNull(userInfoVo)) {
				BeanUtils.copyProperties(userInfoVo, userInfo);
				recipeData.setCreatedBy(userInfo);
			}
			if(vo.isIsPublic()!=null)
			{
				recipeData.setIsPublic(vo.isIsPublic());
			}
			else
			{
				recipeData.setIsPublic(true);
			}
			if (vo.getPlugins() != null) {
				recipeData.setPlugins(vo.getPlugins());
			}
			List<UserInfo> users = new ArrayList<>();
			List<UserInfoVO> userDetails = vo.getUsers();
			if (vo.isIsPublic() == true) {
				users = new ArrayList<>();
			} else {
				
				users = userDetails.stream().map(n -> this.toUserInfo(n)).collect(Collectors.toList());
			}
			// if(userDetails.size()>0)
			// {
			// 	users = userDetails.stream().map(n->this.toUserInfo(n)).collect(Collectors.toList());
			// }
			// else
			// {
			// 	users= new ArrayList<UserInfo>();
			// }
			recipeData.setUsers(users);
			recipeData.setOSName(vo.getOSName().toString());
			entity.setData(recipeData);
		}
		return entity;
	}

	private UserInfoVO toUserInfoVO(UserInfo userInfo) {
		UserInfoVO vo = new UserInfoVO();
		if (userInfo != null) {
			BeanUtils.copyProperties(userInfo, vo);
		}
		return vo;
	}

	public UserInfo toUserInfo(UserInfoVO userInfo) {
		UserInfo entity = new UserInfo();
		if (userInfo != null) {
			BeanUtils.copyProperties(userInfo, entity);
		}
		return entity;
	}

	
	public RecipeLovVO toRecipeLovVO(CodeServerRecipeLov entity)
	{
		RecipeLovVO vo = new RecipeLovVO();
		if(entity!=null || Objects.nonNull(entity))
		{
			// BeanUtils.copyProperties(vo,entity);
			vo.setId(entity.getId());
			vo.setRecipeName(entity.getRecipeName());
		}
		return vo;
	}

	
	public CodeServerRecipeLov toRecipeLovEntity(RecipeLovVO vo)
	{
		CodeServerRecipeLov entity = new CodeServerRecipeLov();
		if(vo!=null || Objects.nonNull(vo))
		{
			// BeanUtils.copyProperties(entity,vo);
			entity.setId(vo.getId());
			entity.setRecipeName(vo.getRecipeName());
		}
		return entity;
	}

}
