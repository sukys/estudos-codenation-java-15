package br.com.codenation.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.codenation.model.OrderItem;
import br.com.codenation.model.Product;
import br.com.codenation.repository.ProductRepository;
import br.com.codenation.repository.ProductRepositoryImpl;

public class OrderServiceImpl implements OrderService {

	private ProductRepository productRepository = new ProductRepositoryImpl();

	/**
	 * Calculate the sum of all OrderItems
	 */
	@Override
	public Double calculateOrderValue(List<OrderItem> items) {
		Double total = 0.0;
		Map<Long, Double> produtos = findProductsById(items.stream().filter(item -> productExists(item.getProductId()))
				.map(OrderItem::getProductId).collect(Collectors.toList())).stream()
						.collect(Collectors.toMap(Product::getId, Product::getValue));
		items.forEach(item -> Double.sum(total, produtos.get(item.getProductId()) * item.getQuantity()));
		return total;
	}

	/**
	 * Map from idProduct List to Product Set
	 */
	@Override
	public Set<Product> findProductsById(List<Long> ids) {
		// ids = ids.stream().filter(id ->
		// productExists(id)).collect(Collectors.toList());
		return productRepository.findAll().stream().parallel().filter(p -> ids.contains(p.getId()))
				.collect(Collectors.toSet());
	}

	/**
	 * Calculate the sum of all Orders(List<OrderIten>)
	 */
	@Override
	public Double calculateMultipleOrders(List<List<OrderItem>> orders) {
		Double total = 0.0;
		orders.forEach(list -> Double.sum(total, calculateOrderValue(list)));
		return total;
	}

	/**
	 * Group products using isSale attribute as the map key
	 */
	@Override
	public Map<Boolean, List<Product>> groupProductsBySale(List<Long> productIds) {
		Map<Boolean, List<Product>> mapa = new HashMap<>();
		Set<Product> produtos = findProductsById(productIds.stream()
				.filter(id -> productExists(id)).collect(Collectors.toList()));
		mapa.put(true, produtos.stream()
				.filter(Product::getIsSale)
				.collect(Collectors.toList()));
		mapa.put(false, produtos.stream()
				.filter(p -> !p.getIsSale())
				.collect(Collectors.toList()));
		return mapa;
	}

	private boolean productExists(Long id) {
		return productRepository.findById(id).isPresent();
	}

}