import json
import uuid
import boto3

products_table_name = 'cmtr-401608dd-dynamodb-l-table-products'
stocks_table_name = 'cmtr-401608dd-dynamodb-l-table-stocks'

dynamodb = boto3.resource('dynamodb')
products_table = dynamodb.Table(products_table_name)
stocks_table = dynamodb.Table(stocks_table_name)

def lambda_handler(event, context):

    uuid = '14ba3d6a-a5ed-491b-a128-0a32b71a38c4'

    if 'headers' in event and 'random-uuid' in event["headers"]:
        uuid += f'-{event["headers"]["random-uuid"]}'

    title = "Lollipop"
    description = "This is a candy product description"
    price = 13
    count = 13

    try:
        products_table.put_item(
            Item={
                'id': uuid,
                'title': title,
                'description': description,
                'price': price
            }
        )

        stocks_table.put_item(
            Item={
                'product_id': uuid,
                'count': count
            }
        )

        return {
            'statusCode': 200,
            'body': json.dumps("Saved!")
        }
    except Exception as e:
        return {
            'statusCode': 500,
            'body': json.dumps(f"Error adding product: {str(e)}")
        }
