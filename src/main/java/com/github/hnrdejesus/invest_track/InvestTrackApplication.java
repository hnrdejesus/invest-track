
package com.github.hnrdejesus.invest_track;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for InvestTrack application.
 *
 * This Spring Boot application provides investment portfolio management
 * with features including:
 * - Real-time market data integration
 * - Monte Carlo simulation for portfolio analysis
 * - Backtesting engine for strategy validation
 * - Financial metrics calculation (Sharpe ratio, volatility, etc.)
 *
 * @author Henrique de Jesus
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
public class InvestTrackApplication {

	/**
	 * Main method to start the Spring Boot application.
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(InvestTrackApplication.class, args);
	}
}