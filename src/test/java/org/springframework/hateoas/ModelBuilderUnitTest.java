/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.hateoas.IanaLinkRelations.*;
import static org.springframework.hateoas.MappingTestUtils.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.collectionjson.Jackson2CollectionJsonModule;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.mediatype.uber.Jackson2UberModule;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Greg Turnquist
 */
public class ModelBuilderUnitTest {

	private ObjectMapper mapper;
	private ContextualMapper contextualMapper;

	@BeforeEach
	void setUp() {

		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new Jackson2HalModule());
		this.mapper.setHandlerInstantiator(new Jackson2HalModule.HalHandlerInstantiator(
				new EvoInflectorLinkRelationProvider(), CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY));
		this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

		this.contextualMapper = createMapper(getClass());
	}

	@Test // #864
	void embeddedSpecUsingHalModelBuilder() throws Exception {

		RepresentationModel<?> model = Model.hal() //
				.embed(LinkRelation.of("author"), Model.builder() //
						.entity(new Author("Alan Watts", "January 6, 1915", "November 16, 1973")) //
						.link(Link.of("/people/alan-watts")) //
						.build())
				.embed(LinkRelation.of("illustrator"), Model.builder() //
						.entity(new Author("John Smith", null, null)) //
						.link(Link.of("/people/john-smith")) //
						.build())
				.link(Link.of("/books/the-way-of-zen")) //
				.link(Link.of("/people/alan-watts", LinkRelation.of("author"))) //
				.link(Link.of("/people/john-smith", LinkRelation.of("illustrator"))) //
				.build();

		assertThat(this.mapper.writeValueAsString(model))
				.isEqualTo(contextualMapper.readFile("hal-embedded-author-illustrator.json"));
	}

	@Test // #864
	void previewForLinkRelationsUsingHalModelBuilder() throws Exception {

		RepresentationModel<?> model = Model.hal() //
				.previewFor(LinkRelation.of("author"), Model.builder() //
						.entity(new Author("Alan Watts", "January 6, 1915", "November 16, 1973")) //
						.link(Link.of("/people/alan-watts")) //
						.build())
				.previewFor(LinkRelation.of("illustrator"), Model.builder() //
						.entity(new Author("John Smith", null, null)) //
						.link(Link.of("/people/john-smith")) //
						.build())
				.link(Link.of("/books/the-way-of-zen")) //
				.link(Link.of("/people/alan-watts", LinkRelation.of("author"))) //
				.link(Link.of("/people/john-smith", LinkRelation.of("illustrator"))) //
				.build();

		assertThat(this.mapper.writeValueAsString(model))
				.isEqualTo(contextualMapper.readFile("hal-embedded-author-illustrator.json"));
	}

	@Test // #864
	void embeddedSpecUsingHalModelBuilderButRenderedAsCollectionJson() throws Exception {

		RepresentationModel<?> model = Model.hal() //
				.embed(LinkRelation.of("author"), Model.builder() //
						.entity(new Author("Alan Watts", "January 6, 1915", "November 16, 1973")) //
						.link(Link.of("/people/alan-watts")) //
						.build())
				.embed(LinkRelation.of("illustrator"), Model.builder() //
						.entity(new Author("John Smith", null, null)) //
						.link(Link.of("/people/john-smith")) //
						.build())
				.link(Link.of("/books/the-way-of-zen")) //
				.link(Link.of("/people/alan-watts", LinkRelation.of("author"))) //
				.link(Link.of("/people/john-smith", LinkRelation.of("illustrator"))) //
				.build();

		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new Jackson2CollectionJsonModule());
		this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

		assertThatExceptionOfType(JsonMappingException.class).isThrownBy(() -> this.mapper.writeValueAsString(model));
	}

	@Test // #864
	void embeddedSpecUsingHalModelBuilderButRenderedAsUber() throws Exception {

		RepresentationModel<?> model = Model.hal() //
				.embed(LinkRelation.of("author"), Model.builder() //
						.entity(new Author("Alan Watts", "January 6, 1915", "November 16, 1973")) //
						.link(Link.of("/people/alan-watts")) //
						.build())
				.embed(LinkRelation.of("illustrator"), Model.builder() //
						.entity(new Author("John Smith", null, null)) //
						.link(Link.of("/people/john-smith")) //
						.build())
				.link(Link.of("/books/the-way-of-zen")) //
				.link(Link.of("/people/alan-watts", LinkRelation.of("author"))) //
				.link(Link.of("/people/john-smith", LinkRelation.of("illustrator"))) //
				.build();

		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new Jackson2UberModule());
		this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

		assertThatExceptionOfType(JsonMappingException.class).isThrownBy(() -> this.mapper.writeValueAsString(model));
	}

	@Test // #864
	void renderSingleItemUsingHalModelBuilder() throws Exception {

		RepresentationModel<?> model = Model.hal() //
				.entity(new Author("Alan Watts", "January 6, 1915", "November 16, 1973")) //
				.link(Link.of("/people/alan-watts")) //
				.build();

		assertThat(this.mapper.writeValueAsString(model)).isEqualTo(contextualMapper.readFile("hal-single-item.json"));
	}

	@Test // #864
	void renderSingleItemUsingDefaultModelBuilder() throws Exception {

		RepresentationModel<?> model = Model.builder()
				.entity(new Author("Alan Watts", "January 6, 1915", "November 16, 1973")) //
				.link(Link.of("/people/alan-watts")) //
				.build();

		assertThat(this.mapper.writeValueAsString(model)).isEqualTo(contextualMapper.readFile("hal-single-item.json"));
	}

	@Test // #864
	void renderCollectionUsingDefaultModelBuilder() throws Exception {

		RepresentationModel<?> model = Model.builder() //
				.entity( //
						Model.builder() //
								.entity(new Author("Greg L. Turnquist", null, null)) //
								.link(Link.of("http://localhost/author/1")) //
								.link(Link.of("http://localhost/authors", LinkRelation.of("authors"))) //
								.build())
				.entity( //
						Model.builder() //
								.entity(new Author("Craig Walls", null, null)) //
								.link(Link.of("http://localhost/author/2")) //
								.link(Link.of("http://localhost/authors", LinkRelation.of("authors"))) //
								.build())
				.entity( //
						Model.builder() //
								.entity(new Author("Oliver Drotbohm", null, null)) //
								.link(Link.of("http://localhost/author/3")) //
								.link(Link.of("http://localhost/authors", LinkRelation.of("authors"))) //
								.build())
				.link(Link.of("http://localhost/authors")) //
				.build();

		assertThat(this.mapper.writeValueAsString(model))
				.isEqualTo(contextualMapper.readFile("hal-embedded-collection.json"));
	}

	@Test // #864
	void renderCollectionUsingHalModelBuilder() throws Exception {

		RepresentationModel<?> model = Model.hal() //
				.entity( //
						Model.builder() //
								.entity(new Author("Greg L. Turnquist", null, null)) //
								.link(Link.of("http://localhost/author/1")) //
								.link(Link.of("http://localhost/authors", LinkRelation.of("authors"))) //
								.build())
				.entity( //
						Model.builder() //
								.entity(new Author("Craig Walls", null, null)) //
								.link(Link.of("http://localhost/author/2")) //
								.link(Link.of("http://localhost/authors", LinkRelation.of("authors"))) //
								.build())
				.entity( //
						Model.builder() //
								.entity(new Author("Oliver Drotbohm", null, null)) //
								.link(Link.of("http://localhost/author/3")) //
								.link(Link.of("http://localhost/authors", LinkRelation.of("authors"))) //
								.build())
				.link(Link.of("http://localhost/authors")) //
				.build();

		assertThat(this.mapper.writeValueAsString(model))
				.isEqualTo(contextualMapper.readFile("hal-embedded-collection.json"));
	}

	@Test
	void progressivelyAddingContentUsingHalModelBuilder() throws JsonProcessingException {

		Model.HalModelBuilder halModelBuilder = Model.hal();

		assertThat(this.mapper.writeValueAsString(halModelBuilder.build()))
				.isEqualTo(contextualMapper.readFile("hal-empty.json"));

		halModelBuilder //
				.entity(Model.builder() //
						.entity(new Author("Greg L. Turnquist", null, null)) //
						.link(Link.of("http://localhost/author/1")) //
						.link(Link.of("http://localhost/authors", LinkRelation.of("authors"))) //
						.build());

		assertThat(this.mapper.writeValueAsString(halModelBuilder.build()))
				.isEqualTo(contextualMapper.readFile("hal-one-thing.json"));

		halModelBuilder //
				.embed(LinkRelation.of("products"), new Product("Alf alarm clock", 19.99)).build();

		assertThat(this.mapper.writeValueAsString(halModelBuilder.build()))
				.isEqualTo(contextualMapper.readFile("hal-two-things.json"));
	}

	@Test // #193
	void renderDifferentlyTypedEntitiesUsingDefaultModelBuilder() throws Exception {

		Staff staff1 = new Staff("Frodo Baggins", "ring bearer");
		Staff staff2 = new Staff("Bilbo Baggins", "burglar");

		Product product1 = new Product("ring of power", 999.99);
		Product product2 = new Product("Saruman's staff", 9.99);

		RepresentationModel<?> model = Model.builder() //
				.entity(staff1) //
				.entity(staff2) //
				.entity(product1) //
				.entity(product2) //
				.link(Link.of("/people/alan-watts")) //
				.build();

		assertThat(this.mapper.writeValueAsString(model)).isEqualTo(contextualMapper.readFile("hal-multiple-types.json"));
	}

	@Test // #193
	void renderDifferentlyTypedEntitiesUsingHalModelBuilder() throws Exception {

		Staff staff1 = new Staff("Frodo Baggins", "ring bearer");
		Staff staff2 = new Staff("Bilbo Baggins", "burglar");

		Product product1 = new Product("ring of power", 999.99);
		Product product2 = new Product("Saruman's staff", 9.99);

		RepresentationModel<?> model = Model.hal() //
				.entity(staff1) //
				.entity(staff2) //
				.entity(product1) //
				.entity(product2) //
				.link(Link.of("/people/alan-watts")) //
				.build();

		assertThat(this.mapper.writeValueAsString(model)).isEqualTo(contextualMapper.readFile("hal-multiple-types.json"));
	}

	@Test // #193
	void renderExplicitAndImplicitLinkRelationsUsingHalModelBuilder() throws Exception {

		Staff staff1 = new Staff("Frodo Baggins", "ring bearer");
		Staff staff2 = new Staff("Bilbo Baggins", "burglar");

		Product product1 = new Product("ring of power", 999.99);
		Product product2 = new Product("Saruman's staff", 9.99);

		RepresentationModel<?> model = Model.hal() //
				.entity(staff1) //
				.entity(staff2) //
				.entity(product1) //
				.entity(product2) //
				.link(Link.of("/people/alan-watts")) //
				.embed(LinkRelation.of("ring bearers"), staff1) //
				.embed(LinkRelation.of("burglars"), staff2) //
				.link(Link.of("/people/frodo-baggins", LinkRelation.of("frodo"))) //
				.build();

		assertThat(this.mapper.writeValueAsString(model))
				.isEqualTo(contextualMapper.readFile("hal-explicit-and-implicit-relations.json"));
	}

	@Test // #175 #864
	void renderZoomProtocolUsingHalModelBuilder() throws JsonProcessingException {

		Map<Integer, ZoomProduct> products = new TreeMap<>();

		products.put(998, new ZoomProduct("someValue", true, true));
		products.put(777, new ZoomProduct("someValue", true, false));
		products.put(444, new ZoomProduct("someValue", false, true));
		products.put(333, new ZoomProduct("someValue", false, true));
		products.put(222, new ZoomProduct("someValue", false, true));
		products.put(111, new ZoomProduct("someValue", false, true));
		products.put(555, new ZoomProduct("someValue", false, true));
		products.put(666, new ZoomProduct("someValue", false, true));

		List<EntityModel<ZoomProduct>> productCollectionModel = products.keySet().stream() //
				.map(id -> EntityModel.of(products.get(id), Link.of("http://localhost/products/{id}").expand(id))) //
				.collect(Collectors.toList());

		LinkRelation favoriteProducts = LinkRelation.of("favorite products");
		LinkRelation purchasedProducts = LinkRelation.of("purchased products");

		Model.HalModelBuilder builder = Model.hal();

		builder.link(Link.of("/products").withSelfRel());

		for (EntityModel<ZoomProduct> productEntityModel : productCollectionModel) {

			if (productEntityModel.getContent().isFavorite()) {

				builder //
						.embed(favoriteProducts, productEntityModel) //
						.link(productEntityModel.getRequiredLink(SELF).withRel(favoriteProducts));
			}

			if (productEntityModel.getContent().isPurchased()) {

				builder //
						.embed(purchasedProducts, productEntityModel) //
						.link(productEntityModel.getRequiredLink(SELF).withRel(purchasedProducts));
			}
		}

		assertThat(this.mapper.writeValueAsString(builder.build()))
				.isEqualTo(contextualMapper.readFile("zoom-hypermedia.json"));
	}

	@Value
	@AllArgsConstructor
	static class Author {

		private String name;
		@Getter(onMethod = @__({ @JsonInclude(JsonInclude.Include.NON_NULL) })) private String born;
		@Getter(onMethod = @__({ @JsonInclude(JsonInclude.Include.NON_NULL) })) private String died;
	}

	@Value
	@AllArgsConstructor
	static class Staff {

		private String name;
		private String role;
	}

	@Value
	@AllArgsConstructor
	static class Product {

		private String name;
		private Double price;
	}

	@Data
	@AllArgsConstructor
	static class ZoomProduct {

		private String someProductProperty;
		@Getter(onMethod = @__({ @JsonIgnore })) private boolean favorite = false;
		@Getter(onMethod = @__({ @JsonIgnore })) private boolean purchased = false;
	}
}