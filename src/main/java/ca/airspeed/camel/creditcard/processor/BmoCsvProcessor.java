package ca.airspeed.camel.creditcard.processor;

import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import static org.apache.commons.io.IOUtils.readLines;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class BmoCsvProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		File txnFile = exchange.getIn().getBody(File.class);
		Reader in = new FileReader(txnFile);
		List<String> lines = readLines(in);
		lines.remove(0); // "Following data is valid as of..."
		lines.remove(0); // The blank line before the header.
		StringBuilder bld = new StringBuilder();
		boolean isFirstLine = true;
		for (String line : lines) {
			if (isFirstLine) {
				isFirstLine = false;
			} else {
				bld.append(LINE_SEPARATOR);
			}
			bld.append(line);
		}
		Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(new StringReader(bld.toString()));
		List<Map<String, ?>> items = new ArrayList<>();
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		for (CSVRecord record : records) {
			Map<String, Object> item = new HashMap<>();
			item.put("txnDate", df.parse(record.get("Transaction Date")));
			item.put("txnAmount", new BigDecimal(record.get("Transaction Amount")));
			item.put("description", record.get("Description"));
			items.add(item);
		}
		exchange.getIn().setBody(items);
	}

}
