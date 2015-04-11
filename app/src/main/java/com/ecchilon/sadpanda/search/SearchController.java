package com.ecchilon.sadpanda.search;

import java.util.List;

import android.net.Uri;
import com.ecchilon.sadpanda.R;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Data;
import roboguice.util.Strings;

@Data
public class SearchController {
	private static final String STAR_PARAM = "f_srdd";
	private static final String SEARCH_PARAM = "f_search";
	private static final String AUTHORITY = "exhentai.org";
	private static final String SCHEME = "http";

	private static final ImmutableList<CategoryObject> CATEGORY_PARAMS =
			new ImmutableList.Builder<CategoryObject>().add(
					(CategoryObject) new CategoryObject("f_doujinshi", "1", "0", R.color.doujinshi,
							R.string.doujinshi).setActive(true),
					(CategoryObject) new CategoryObject("f_manga", "1", "0", R.color.manga, R.string.manga).setActive(
							true),
					(CategoryObject) new CategoryObject("f_artistcg", "1", "0", R.color.artist_cg,
							R.string.artist_cg).setActive(true),
					(CategoryObject) new CategoryObject("f_gamecg", "1", "0", R.color.game_cg,
							R.string.game_cg).setActive(true),
					(CategoryObject) new CategoryObject("f_western", "1", "0", R.color.western,
							R.string.western).setActive(true),
					(CategoryObject) new CategoryObject("f_non-h", "1", "0", R.color.non_h, R.string.nonh).setActive(
							true),
					(CategoryObject) new CategoryObject("f_imageset", "1", "0", R.color.image_set,
							R.string.imageset).setActive(true),
					(CategoryObject) new CategoryObject("f_cosplay", "1", "0", R.color.cosplay,
							R.string.cosplay).setActive(true),
					(CategoryObject) new CategoryObject("f_asianporn", "1", "0", R.color.asian_porn, R.string
							.asianporn)
							.setActive(
									true),
					(CategoryObject) new CategoryObject("f_misc", "1", "0", R.color.misc, R.string.misc).setActive(
							true)).build();

	private static final ImmutableList<QueryObject> QUERY_PARAMS = new ImmutableList.Builder<QueryObject>().add(
			new QueryObject("f_sname", "on", "", R.string.by_name).setActive(true),
			new QueryObject("f_stags", "on", "", R.string.by_tag).setActive(false),
			new QueryObject("f_sh", "on", "", R.string.by_expunged).setActive(false),
			new QueryObject("f_sdesc", "on", "", R.string.by_desc).setActive(false),
			new QueryObject("f_sr", "on", "", R.string.by_star).setActive(false)).build();


	private final List<QueryObject> queryParameters =
			Lists.newArrayListWithCapacity(QUERY_PARAMS.size() + CATEGORY_PARAMS.size());

	private int stars = 0;

	public SearchController(String url) {
		Uri uri = Uri.parse(url);

		for (CategoryObject entry : CATEGORY_PARAMS) {
			CategoryObject copy = entry.copy();
			String param = uri.getQueryParameter(copy.getKey());
			if(param != null) {
				if(param.equals(copy.getOnValue())) {
					copy.setActive(true);
				}
				else if(param.equals(copy.getOffValue())) {
					copy.setActive(false);
				}
			}
			queryParameters.add(copy);
		}

		for (QueryObject entry : QUERY_PARAMS) {
			QueryObject copy = entry.copy();
			String param = uri.getQueryParameter(copy.getKey());
			if(param != null) {
				if(param.equals(copy.getOnValue())) {
					copy.setActive(true);
				}
				else if(param.equals(copy.getOffValue())) {
					copy.setActive(false);
				}
			}
			queryParameters.add(copy);
		}
	}

	public SearchController() {
		for (CategoryObject entry : CATEGORY_PARAMS) {
			CategoryObject copy = entry.copy();
			queryParameters.add(copy);
		}

		for (QueryObject entry : QUERY_PARAMS) {
			QueryObject copy = entry.copy();
			queryParameters.add(copy);
		}
	}

	public String getUrl(String query) {
		return buildUrl(queryParameters, query);
	}

	public static String getDefaultUrl(String query) {
		List<QueryObject> list = Lists.newArrayList(QUERY_PARAMS);
		list.addAll(CATEGORY_PARAMS);
		return buildUrl(list, query);
	}

	private static String buildUrl(List<QueryObject> queryObjects, String query) {
		Uri.Builder builder = new Uri.Builder().scheme(SCHEME).authority(AUTHORITY).path("/");
		for (QueryObject queryObj : queryObjects) {
			if (!Strings.isEmpty(queryObj.getValue())) {
				builder.appendQueryParameter(queryObj.getKey(), queryObj.getValue());
			}
		}

		builder.appendQueryParameter(SEARCH_PARAM, query);

		builder.appendQueryParameter(STAR_PARAM, Integer.toString(0));

		return builder.build().toString();
	}

	public static String getUploaderUrl(String uploader) {
		Uri.Builder builder = new Uri.Builder().scheme(SCHEME).authority(AUTHORITY).path("/uploader/" + uploader);
		return builder.build().toString();
	}

	public void reset() {
		int catSize = CATEGORY_PARAMS.size(), querySize = QUERY_PARAMS.size();
		for (int i = 0; i < catSize; i++) {
			queryParameters.get(i).setActive(CATEGORY_PARAMS.get(i).isActive());
		}

		for (int i = 0; i < querySize; i++) {
			queryParameters.get(catSize + i).setActive(QUERY_PARAMS.get(i).isActive());
		}

		stars = 0;
	}
}
