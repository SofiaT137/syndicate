import json
import boto3
from decimal import Decimal

products_table_name = 'cmtr-401608dd-dynamodb-l-table-products'
stocks_table_name = 'cmtr-401608dd-dynamodb-l-table-stocks'

dynamodb = boto3.resource('dynamodb')
products_table = dynamodb.Table(products_table_name)
stocks_table = dynamodb.Table(stocks_table_name)

def decimal_default(obj):
    if isinstance(obj, Decimal):
        return str(obj)
    raise TypeError

def lambda_handler(event, context):

    uuid = '14ba3d6a-a5ed-491b-a128-0a32b71a38c4'

    if 'headers' in event and 'random-uuid' in event["headers"]:
        uuid += f'-{event["headers"]["random-uuid"]}'

    product_response = products_table.get_item(
        Key={
            'id': uuid
        }
    )

    print(product_response)

    stock_response = stocks_table.get_item(
        Key={
            'product_id': uuid
        }
    )

    print(stock_response)

    if 'Item' not in product_response or 'Item' not in stock_response:
        return {
            'statusCode': 404,
            'body': json.dumps('Product not found')
        }

    product_item = product_response['Item']
    stock_item = stock_response['Item']

    response = {
        'id': {'S': product_item['id']},
        'title': {'S': product_item['title']},
        'description': {'S': product_item['description']},
        'price': {'N': str(product_item['price'])},
        'count': {'N': str(stock_item['count'])}
    }

    return {
        'statusCode': 200,
        'body': json.dumps(response)
    }
